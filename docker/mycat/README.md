`docker-entrypoint.sh`: Load MySQL configurations from environment variables, and start mycat server in foreground mode.

#### 前台启动Mycat
- `openjdk:8`: Use `bin/mycat console` to run in foreground mode.
  > `openjdk:8`: OS is `Debian 10.2`
- `openjdk:8-jre-alpine`: `bin/mycat console` throws following exceptions:
  ```
  Unable to locate any of the following operational binaries:
    /home/mycat/bin/./wrapper-linux-x86-64 (Found but not executable.)
    /home/mycat/bin/./wrapper-linux-x86-32 (Found but not executable.)
    /home/mycat/bin/./wrapper
  ```
  Use `bin/startup_nowrap.sh` instead.
  > `wrapper-linux-x86-64` provided by Mycat can't run in `Alpine Linux`
- `bin/startup_nowrap.sh`：
  1. Remove `JAVA_OPTS` to reduce memory allocations.
  2. Change from daemon run to foreground mode.