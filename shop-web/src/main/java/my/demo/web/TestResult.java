package my.demo.web;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
public class TestResult {
	private int count;
	private long elapsed;
	private long elapsedAvg;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Shanghai")
	private Date startAt = new Date();
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Shanghai")
	private Date stopAt;
	private int success;
	private int failed;
	
	public TestResult stop() {
		this.stopAt = new Date();
		this.elapsed = this.stopAt.getTime() - this.startAt.getTime();
		this.elapsedAvg = this.elapsed / this.count;
		return this;
	}
	public TestResult success() {
		this.count++;
		this.success++;
		return this;
	}
	public TestResult fail() {
		this.failed++;
		this.count++;
		return this;
	}
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public long getElapsed() {
		return elapsed;
	}
	public void setElapsed(long elapsed) {
		this.elapsed = elapsed;
	}
	public long getElapsedAvg() {
		return elapsedAvg;
	}
	public void setElapsedAvg(long elapsedAvg) {
		this.elapsedAvg = elapsedAvg;
	}
	public Date getStartAt() {
		return startAt;
	}
	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}
	public Date getStopAt() {
		return stopAt;
	}
	public void setStopAt(Date stopAt) {
		this.stopAt = stopAt;
	}
	public int getSuccess() {
		return success;
	}
	public void setSuccess(int success) {
		this.success = success;
	}
	public int getFailed() {
		return failed;
	}
	public void setFailed(int failed) {
		this.failed = failed;
	}
}