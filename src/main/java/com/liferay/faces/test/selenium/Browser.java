/**
 * Copyright (c) 2000-2016 Liferay, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.liferay.faces.test.selenium;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * @author  Kyle Stiemann
 */
public class Browser implements WebDriver {

	private static WebDriver webDriver = null;
	private static WebDriverWait wait = null;
	private static Browser instance = null;
	private static final Logger logger = Logger.getLogger(Browser.class.getName());

	static {

		String defaultLogLevel = "WARNING";

		if (!IntegrationTesterBase.RUNNING_WITH_MAVEN_SUREFIRE_PLUGIN) {
			defaultLogLevel = "FINE";
		}

		String logLevelString = TestUtil.getSystemPropertyOrDefault("integration.log.level", defaultLogLevel);
		Level logLevel = Level.parse(logLevelString);
		logger.setLevel(logLevel);
	}

	private final String NAME;

	private Browser() {

		String defaultBrowser = "phantomjs";

		if (!IntegrationTesterBase.RUNNING_WITH_MAVEN_SUREFIRE_PLUGIN) {
			defaultBrowser = "firefox";
		}

		NAME = TestUtil.getSystemPropertyOrDefault("integration.browser", defaultBrowser);

		if ("phantomjs".equals(NAME)) {
			webDriver = new PhantomJSDriver();
		}
		else if ("chrome".equals(NAME)) {
			webDriver = new ChromeDriver();
		}
		else if ("firefox".equals(NAME)) {
			webDriver = new FirefoxDriver();
		}

		webDriver.manage().window().maximize();
		wait = new WebDriverWait(webDriver, 5);
	}

	public static Browser getInstance() {

		if (instance == null) {
			instance = new Browser();
		}

		return instance;
	}

	public void centerElementInView(String xpath) {

		// http://stackoverflow.com/questions/8922107/javascript-scrollintoview-middle-alignment#36499256
		executeScript(
			"window.scrollTo(0, (arguments[0].getBoundingClientRect().top + window.pageYOffset) - (window.innerHeight / 2))",
			findElementByXpath(xpath));
	}

	public void click(String xpath) {
		findElementByXpath(xpath).click();
	}

	/**
	 * Clicks on the element specified via xpath and waits for the clicked element to be rerendered via Ajax. This
	 * method will only work if the element clicked is also rerendered via Ajax. If the clicked element will not be
	 * rerendered via Ajax, then use {@link
	 * Browser#performAndWaitForAjaxRerender(org.openqa.selenium.interactions.Action, java.lang.String)} with {@link
	 * Browser#createClickAction(java.lang.String)} and the xpath of an element which will be rerendered instead.
	 *
	 * @param  xpath  The xpath of the element to be clicked and rerendered.
	 */
	public void clickAndWaitForAjaxRerender(String xpath) {
		performAndWaitForAjaxRerender(createClickAction(xpath), xpath);
	}

	@Override
	public void close() {
		webDriver.close();
	}

	// Currently unused:
	public Actions createActions() {
		return new Actions(webDriver);
	}

	public Action createClickAction(String xpath) {

		Actions actions = createActions();
		WebElement element = findElementByXpath(xpath);
		actions.click(element);

		return actions.build();
	}

	// Currently unused:
	public Action createSendKeysAction(String xpath, CharSequence... keys) {

		Actions actions = createActions();
		WebElement element = findElementByXpath(xpath);
		actions.sendKeys(element, keys);

		return actions.build();
	}

	// Currently unused:
	public Object executeAsyncScript(String script, Object... args) {

		JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;

		return javascriptExecutor.executeAsyncScript(script, args);
	}

	// Currently unused:
	public Object executeScript(String script, Object... args) {

		JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;

		return javascriptExecutor.executeScript(script, args);
	}

	@Override
	public WebElement findElement(By by) {
		return webDriver.findElement(by);
	}

	public WebElement findElementByXpath(String xpath) {
		return findElement(By.xpath(xpath));
	}

	@Override
	public List<WebElement> findElements(By by) {
		return webDriver.findElements(by);
	}

	@Override
	public void get(String url) {
		webDriver.get(url);
	}

	@Override
	public Options manage() {
		return webDriver.manage();
	}

	@Override
	public Navigation navigate() {
		return webDriver.navigate();
	}

	/**
	 * Performs an {@link Action} and waits for an element to be rerendered via Ajax.
	 *
	 * @param  action         The action which will cause the Ajax rerender.
	 * @param  rerenderXpath  The xpath of the element which will be rerendered.
	 */
	public void performAndWaitForAjaxRerender(Action action, String rerenderXpath) {

		WebElement rerenderElement = findElementByXpath(rerenderXpath);
		action.perform();
		logger.log(Level.INFO, "Waiting for element {0} to be stale.", rerenderXpath);
		waitUntil(ExpectedConditions.stalenessOf(rerenderElement));
		logger.log(Level.INFO, "Element {0} is stale.", rerenderXpath);
		waitForElementVisible(rerenderXpath);
	}

	@Override
	public void quit() {
		webDriver.quit();
	}

	public void sendKeys(String xpath, CharSequence... keys) {
		findElementByXpath(xpath).sendKeys(keys);
	}

	// Currently unused:
	/**
	 * Sends keys to the element specified via xpath and waits for the element to be rerendered via Ajax. This method
	 * will only work if the element receiving the keys is also rerendered via Ajax. If the element receiving the keys
	 * will not be rerendered via Ajax, then use {@link
	 * Browser#performAndWaitForAjaxRerender(org.openqa.selenium.interactions.Action, java.lang.String)} with {@link
	 * Browser#createSendKeysAction(java.lang.String, java.lang.CharSequence...)} and the xpath of an element which will
	 * be rerendered instead.
	 *
	 * @param  xpath  The xpath of the element to be clicked and rerendered.
	 * @param  keys   The keys to be sent.
	 */
	public void sendKeysAndWaitForAjaxRerender(String xpath, CharSequence... keys) {
		performAndWaitForAjaxRerender(createSendKeysAction(xpath, keys), xpath);
	}

	@Override
	public TargetLocator switchTo() {
		return webDriver.switchTo();
	}

	// Currently unused:
	public void waitForElementNotPresent(String xpath) {

		logger.log(Level.INFO, "Waiting for element {0} to not be present in the DOM.", xpath);
		waitUntil(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.ByXPath.xpath(xpath))));
		logger.log(Level.INFO, "Element {0} is not present in the DOM.", xpath);
	}

	// Currently unused:
	public void waitForElementPresent(String xpath) {

		logger.log(Level.INFO, "Waiting for element {0} to be present in the DOM.", xpath);
		waitUntil(ExpectedConditions.presenceOfElementLocated(By.ByXPath.xpath(xpath)));
		logger.log(Level.INFO, "Element {0} is present in the DOM.", xpath);
	}

	// Currently unused:
	public void waitForElementTextVisible(String xpath, String text) {

		String[] loggerArgs = new String[] { xpath, text };
		waitForElementVisible(xpath);
		logger.log(Level.INFO, "Waiting for element {0} to contain text \"{1}\".", loggerArgs);
		waitUntil(ExpectedConditions.textToBePresentInElementLocated(By.ByXPath.xpath(xpath), text));
		logger.log(Level.INFO, "Element {0} is visible and contains text \"{1}\".", loggerArgs);
	}

	// Currently unused:
	public void waitForElementValue(String xpath, String value) {

		String[] loggerArgs = new String[] { xpath, value };
		waitForElementVisible(xpath);
		logger.log(Level.INFO, "Waiting for element {0} to contain value \"{1}\".", loggerArgs);
		waitUntil(ExpectedConditions.textToBePresentInElementValue(By.ByXPath.xpath(xpath), value));
		logger.log(Level.INFO, "Element {0} is visible and contains value \"{1}\".", loggerArgs);
	}

	public void waitForElementVisible(String xpath) {

		logger.log(Level.INFO, "Waiting for element {0} to be visible.", xpath);
		waitUntil(ExpectedConditions.visibilityOfElementLocated(By.ByXPath.xpath(xpath)));
		logger.log(Level.INFO, "Element {0} is visible.", xpath);
	}

	public void waitUntil(ExpectedCondition expectedCondition) {
		wait.until(expectedCondition);
	}

	@Override
	public String getCurrentUrl() {
		return webDriver.getCurrentUrl();
	}

	public String getName() {
		return NAME;
	}

	@Override
	public String getPageSource() {
		return webDriver.getPageSource();
	}

	@Override
	public String getTitle() {
		return webDriver.getTitle();
	}

	@Override
	public String getWindowHandle() {
		return webDriver.getWindowHandle();
	}

	@Override
	public Set<String> getWindowHandles() {
		return webDriver.getWindowHandles();
	}
}
