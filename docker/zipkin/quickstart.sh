#!/usr/bin/env bash

set -euo pipefail

# This will be set to 1 if we instruct the user to manually verify signatures,
# when they have GPG but don't have the public key of the signer. Would be super
# confusing to tell the user to use files that we've cleaned up.
DO_CLEANUP=0

# shellcheck disable=SC2015
color_title=$(tput setaf 7 && tput bold || true)
color_dim=$(tput setaf 8 || true)
color_good=$(tput setaf 2 || true)
color_bad=$(tput setaf 1 || true)
color_warn=$(tput setaf 3 || true)
color_reset=$(tput sgr0 || true)

#repo=https://repo1.maven.org/maven2
repo=http://maven.aliyun.com/nexus/content/groups/public

usage() {
    cat <<EOF
${color_title}$0${color_reset}
Downloads the latest version of the Zipkin Server executable jar

${color_title}$0 GROUP:ARTIFACT:VERSION:CLASSIFIER TARGET${color_reset}
Downloads the "VERSION" version of GROUP:ARTIFACT with classifier "CLASSIFIER"
to path "TARGET" on the local file system. "VERSION" can take the special value
"LATEST", in which case the latest Zipkin release will be used. For example:

${color_title}$0 io.zipkin.aws:zipkin-autoconfigure-collector-kinesis:LATEST:module kinesis.jar${color_reset}
downloads the latest version of the artifact with group "io.zipkin.aws",
artifact id "zipkin-autoconfigure-collector-kinesis", and classifier "module"
to PWD/kinesis.jar
EOF
}

welcome() {
    cat <<EOF
${color_title}Thank you for trying Zipkin!${color_reset}
This installer is provided as a quick-start helper, so you can try Zipkin out
without a lengthy installation process.

EOF
}

farewell() {
    local artifact_classifier="$1"; shift
    local filename="$1"; shift
    if [[ "$artifact_classifier" = 'exec' ]]; then
        cat <<EOF
${color_good}
You can now run the downloaded executable jar:

    java -jar $filename
${color_reset}
EOF
    else
        cat << EOF
${color_good}
The downloaded artifact is now available at $filename.${color_reset}
EOF
    fi
}

cleanup() {
    local base_filename="$1"; shift
    if [[ "$DO_CLEANUP" -eq 0 ]]; then
        printf '\n%s\n' "${color_title}Cleaning up checksum and signature files${color_reset}"
        execute_and_log rm -f "$base_filename"{.md5,.asc}
        DO_CLEANUP=1
    fi
}

handle_shutdown() {
    local status=$?
    local base_filename="$1"; shift
    if [[ $status -eq 0 ]]; then
        cleanup "$base_filename"
    else
        cat <<EOF
${color_bad}
It looks like quick-start setup has failed. Please run the command again
with the debug flag like below, and open an issue on
https://github.com/openzipkin/openzipkin.github.io/issues/new. Make sure
to include the full output of the run.
${color_reset}
    \\curl -sSL https://zipkin.io/quickstart.sh | bash -sx -- $@

In the meanwhile, you can manually download and run the latest executable jar
from the following URL:

https://search.maven.org/remote_content?g=io.zipkin&a=zipkin-server&v=LATEST&c=exec
EOF
    fi
}

execute_and_log() {
    local command=("$@")
    printf >&2 '%s\n' "${color_dim}> ${command[*]}${color_reset}"
    eval "${command[@]}"
}

fetch() {
    url="$1"; shift
    target="$1"; shift
    execute_and_log curl -fL -o "'$target'" "'$url'"
}

fetch_latest_version() {
    local artifact_group="$1"; shift
    local artifact_id="$1"; shift
    local url="${repo}/${artifact_group_with_slashes}/${artifact_id}/maven-metadata.xml"
    printf $(curl -sSL $url | sed -n '/<version>/s/.*<version>\([^<]*\)<\/version>.*/\1/p'|tail -1)
}

artifact_part() {
    local index="$1"; shift
    local artifact="$1"; shift
    local parts
    IFS=':' read -ra parts <<< "$artifact"
    if [[ "${#parts[@]}" -lt $((index + 1)) ]]; then
        printf ''
    else
        printf '%s' "${parts[$index]}"
    fi
}

verify_version_number() {
    if [[ ! "$1" =~ ^[[:digit:]+\.[[:digit:]]+\.[[:digit:]]+$ ]]; then
        cat <<EOF
${color_bad}The target version is "$1". That doesn't look like a valid Zipkin release version
number; this script is confused. Bailing out.
${color_reset}
EOF
        exit 1
    fi
}

verify_checksum() {
    local url="$1"; shift
    local filename="$1"; shift

    printf '\n%s\n' "${color_title}Verifying checksum...${color_reset}"

    # Fetch the .md5 file even if md5sum is not on the path
    # This lets us verify its GPG signature later on, and the user might have another way of checksum verification
    fetch "$url.md5" "$filename.md5"

    if command -v md5sum >/dev/null 2>&1; then
        execute_and_log "md5sum -c <<< \"\$(cat $filename.md5)  $filename\""
        printf '%s\n' "${color_good}Checksum for ${filename} passes verification${color_reset}"
    else
        printf '%s\n' "${color_warn}md5sum not found on path, skipping checksum verification${color_reset}"
    fi
}

verify_signature() {
    local url="$1"; shift
    local filename="$1"; shift

    printf '\n%s\n' "${color_title}Verifying GPG signature of $filename...${color_reset}"

    local gpg_key='D401AB61'

    if command -v gpg >/dev/null 2>&1; then
        fetch "$url.asc" "$filename.asc"
        if gpg --list-keys "$gpg_key" >/dev/null 2>&1; then
            execute_and_log gpg --verify "$filename.asc" "$filename"
            printf '%s\n' "${color_good}GPG signature for ${filename} passes verification${color_reset}"
        else
            cat <<EOF
${color_warn}
GPG signing key is not known, skipping signature verification.
Use the following commands to manually verify the signature of $filename:

    gpg --keyserver keyserver.ubuntu.com --recv $gpg_key
    # Optionally trust the key via 'gpg --edit-key $gpg_key', then typing 'trust',
    # choosing a trust level, and exiting the interactive GPG session by 'quit'
    gpg --verify $filename.asc $filename
${color_reset}
EOF
            DO_CLEANUP=1
        fi
    else
        printf '%s\n' "${color_warn}gpg not found on path, skipping checksum verification${color_reset}"
    fi
}

main() {
    local artifact_group=io.zipkin
    local artifact_id=zipkin-server
    #local artifact_version=LATEST
    #local artifact_version_lowercase=latest
	local artifact_version=2.19.2
    local artifact_version_lowercase=2.19.2
    local artifact_classifier=exec
    local artifact_group_with_slashes
    local artifact_url

    if [[ $# -eq 0 ]]; then
        local filename="zipkin.jar"
        # shellcheck disable=SC2064
        trap "handle_shutdown \"$filename\" $*" EXIT
    elif [[ "$1" = '-h' || "$1" = '--help' ]]; then
        usage
        exit
    elif [[ $# -eq 2 ]]; then
        local artifact="$1"
        local filename="$2"
        # shellcheck disable=SC2064
        trap "handle_shutdown \"$filename\" $*" EXIT
        artifact_group="$(artifact_part 0 "$artifact")"
        artifact_id="$(artifact_part 1 "$artifact")"
        artifact_version="$(artifact_part 2 "$artifact")"
        artifact_classifier="$(artifact_part 3 "$artifact")"
    else
        usage
        exit 1
    fi

    if [[ -n "$artifact_classifier" ]]; then
        artifact_classifier_suffix="-$artifact_classifier"
    else
        artifact_classifier_suffix=''
    fi

    welcome

    if [ "${artifact_group}" = 'io.zipkin.java' ]; then
       printf '%s\n' "${color_warn}You've requested the server's old group name: 'io.zipkin.java'. Please update links to the current group 'io.zipkin'...${color_reset}"
       artifact_group=io.zipkin
    fi

    artifact_group_with_slashes="${artifact_group//.//}"
    artifact_version_lowercase="$(tr '[:upper:]' '[:lower:]' <<< "$artifact_version")"
    if [  "${artifact_version_lowercase}" = 'latest' ]; then
        printf '%s\n' "${color_title}Fetching version number of latest ${artifact_group}:${artifact_id} release...${color_reset}"
        artifact_version="$(fetch_latest_version "$artifact_group" "$artifact_id")"
    fi
    verify_version_number "$artifact_version"
    printf '%s\n\n' "${color_good}Latest release of ${artifact_group}:${artifact_id} seems to be ${artifact_version}${color_reset}"

    printf '%s\n' "${color_title}Downloading $artifact_group:$artifact_id:$artifact_version:$artifact_classifier to $filename...${color_reset}"
    artifact_url="${repo}/${artifact_group_with_slashes}/${artifact_id}/$artifact_version/${artifact_id}-${artifact_version}${artifact_classifier_suffix}.jar"
    fetch "$artifact_url" "$filename"
    verify_checksum "$artifact_url" "$filename"
    verify_signature "$artifact_url" "$filename"

    cleanup "$filename"
    farewell "$artifact_classifier" "$filename"
}

main "$@"
