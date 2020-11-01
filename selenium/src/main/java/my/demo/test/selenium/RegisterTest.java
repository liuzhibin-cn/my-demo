package my.demo.test.selenium;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Random;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RegisterTest {
	private WebDriver driver;
	private Random random = new Random(System.currentTimeMillis());
	private static String[] phonePrefix = { "131", "132", "133", "135", "136", "137", "138", "139", "156", "157", "158", "159", "186" }; 
	
	@Before
	public void setUp() {
		//Specify the path of webdriver
		System.setProperty("webdriver.chrome.driver", "/Users/richie-home/Documents/apps/selenium/chromedriver");
		driver = new ChromeDriver();
	}
	
	@After
	public void tearDown() {
		driver.quit();
	}
	
	@Test
	public void register() {
		String phone = generateRandomPhoneNumber(), password = "qwe123", userId = "", nickname = "";
		driver.get("http://localhost:18090/shop");
		
		//Register a user
		System.out.println("[register] > phone: " + phone + ", password: " + password);
		driver.findElement(By.id("txtMobile")).sendKeys(phone);
		driver.findElement(By.id("txtPassword")).sendKeys(password);
		driver.findElement(By.id("btnRegister")).click();
		waitUntilRegisterAndLoginFinished();
		
		//Get user id and nickname
		userId = driver.findElement(By.id("labelUserId")).getText();
		nickname = driver.findElement(By.id("labelNickname")).getText();
		System.out.println("[register] < id: " + userId + ", nickname: " + nickname);
		System.out.println("[register] < message: " + driver.findElement(By.id("labelLoginError")).getText());
		assertThat("Failed to register user: " + driver.findElement(By.id("labelLoginError")).getText(), userId!=null && !userId.isEmpty());
		
		//Login
		System.out.println("[login] > phone: " + phone + ", password: " + password);
		driver.findElement(By.id("btnLogin")).click();
		waitUntilRegisterAndLoginFinished();
		System.out.println("[login] < id: " + driver.findElement(By.id("labelUserId")).getText() 
			+ ", nickname: " + driver.findElement(By.id("labelNickname")).getText());
		System.out.println("[login] < message: " + driver.findElement(By.id("labelLoginError")).getText());
		assertThat(driver.findElement(By.id("labelUserId")).getText(), is(userId));
		assertThat(driver.findElement(By.id("labelNickname")).getText(), is(nickname));
	}
	
	private String generateRandomPhoneNumber() {
		String phone = phonePrefix[Math.abs(random.nextInt()) % phonePrefix.length];
		while(true) {
			phone = phone + (Math.abs(random.nextInt()) % 10);
			if(phone.length()>=11) break;
		}
		return phone;
	}
	private void waitUntilRegisterAndLoginFinished() {
		//等待页面登录事件处理完成
		//点击登录按钮btnLogin之后，页面进行AJAX调用，登录成功在labelUserId和labelNickname中输出用户ID和昵称，
		//登录失败则在labelLoginError中显示失败消息
		new WebDriverWait(driver, 3).until( ExpectedConditions.or(
			ExpectedConditions.textMatches(By.id("labelUserId"), Pattern.compile("[a-zA-Z0-9]+")),
			ExpectedConditions.textMatches(By.id("labelLoginError"), Pattern.compile("[a-zA-Z0-9]+"))
		) );
	}
}