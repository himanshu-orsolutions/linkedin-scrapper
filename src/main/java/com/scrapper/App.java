package com.scrapper;

import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class App {
	public static void main(String[] args) {

		System.setProperty("webdriver.chrome.driver", "chromedriver");
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("https://www.linkedin.com/login?trk=guest_homepage-basic_nav-header-signin");

		// Logging in
		driver.findElement(By.id("username")).sendKeys("YOUR_EMAIL");
		driver.findElement(By.id("password")).sendKeys("YOUR_PASSWORD");
		driver.findElement(By.className("login__form")).findElement(By.cssSelector("[aria-label=\"Sign in\"]")).click();

		// Going to search page
		driver.get("https://www.linkedin.com/search/results/all");

		// Opening advanced filters panel and filling necessary information
		driver.findElement(By.className("search-filters-bar__all-filters")).click();
		driver.findElement(By.id("search-advanced-firstName")).sendKeys("FIRSTNAME_TO_SEARCH");
		driver.findElement(By.id("search-advanced-lastName")).sendKeys("LASTNAME_TO_SEARCH");
		driver.findElement(By.className("search-advanced-facets__button--apply")).click();

		String baseLink = driver.getCurrentUrl();

		while (true) {
			String currentLink = driver.getCurrentUrl();
			if (!StringUtils.equals(currentLink, baseLink)) {
				baseLink = currentLink;
				break;
			}

			// Sleeping for 1 second
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException interruptedException) {
				System.out.println("Error: Wait process interrupted.");
			}
		}

		// Iterating over all pages
		int pageNumber = 1;
		Boolean found = false;

		do {
			if (pageNumber > 1) {
				String link = baseLink + "&page=" + pageNumber;
				driver.get(link);
			}
			// Getting all hrefs from the searched results
			HashSet<String> links = new HashSet<>();

			while (true) {
				WebElement list = driver.findElement(By.className("search-results__list"));
				List<WebElement> anchors = list.findElements(By.className("search-result__result-link"));
				((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 500)");

				// Scrolling down until all results are not viewed
				if (anchors.size() < 20) {
					try {
						Thread.sleep(1000l);
					} catch (InterruptedException interruptedException) {
						System.out.println("Error: Wait process interrupted.");
					}
					continue;
				}

				anchors.forEach(anchor -> links.add(anchor.getAttribute("href")));
				break;
			}

			if (!links.isEmpty()) {
				found = true;
				// Opening hrefs one by one
				links.forEach(link -> {
					driver.get(link + "detail/contact-info");
					try {
						WebElement emailElement = driver.findElement(By.className("ci-email"));
						WebElement emailLink = emailElement.findElement(By.className("pv-contact-info__contact-link"));
						System.out.println(emailLink.getAttribute("href"));
					} catch (Exception exception) {
						System.out.println("No email found at " + link);
					}
				});
			} else {
				found = false;
			}
			pageNumber++;
		} while (found);
	}
}
