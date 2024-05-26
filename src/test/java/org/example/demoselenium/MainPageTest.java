package org.example.demoselenium;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainPageTest {
    private WebDriver driver;
    private MainPage mainPage;
    Actions actions;
    private static final String URL = "https://www.zehrs.ca/";
    private static final int MAX_PER_PAGE = 5;

    private void waitInSeconds(int seconds) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(seconds));
    }

    private String[] getProductInfo(WebElement projectNode) {
        String name = projectNode.findElement(By.cssSelector("h3[data-testid=\"product-title\"]")).getText();
        String price = "";
        // try getting regular price
        try {
            price = projectNode.findElement(By.cssSelector("span[data-testid=\"regular-price\"] > span")).getText();
        } catch (NoSuchElementException e) {
            // do nothing
        }
        if (price.isEmpty()) {
            try {
                price = projectNode.findElement(By.cssSelector("span[data-testid=\"non-members-price\"] > span")).getText();
            } catch (NoSuchElementException e) {
                // do nothing
            }
            if (price.isEmpty()) {
                price = projectNode.findElement(By.cssSelector("span[data-testid=\"sale-price\"] > span")).getText();
            }
        }

        String imageUrl = projectNode.findElement(By.cssSelector("img.chakra-image")).getAttribute("src");
        return new String[] {name, price, imageUrl};
    }

    private int writeProductInfoToCSV(CSVWriter writer, int startIndex) throws IOException {
        List<WebElement> products = mainPage.products;
        int count = startIndex;
        int limitCheck = 1;
        // Loop through products and extract data
        for (WebElement product : products) {
            String[] productInfo = getProductInfo(product);
            String[] data = {String.valueOf(count++), productInfo[0], productInfo[1], productInfo[2]};
            writer.writeNext(data);
            limitCheck++;
            if (limitCheck > MAX_PER_PAGE) {
                break;
            }
        }
        return count;
    }

    private void switchToNewPage(int pageNumber) {
        WebElement nextPage = driver.findElement(By.cssSelector("a[aria-label=\"Page " + pageNumber + "\"]"));
        actions.moveToElement(nextPage).perform();
        waitInSeconds(5);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", nextPage);

        waitInSeconds(5);
        WebElement heading = driver.findElement(By.cssSelector("h1[data-testid=\"heading\"]"));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOf(heading));
        assertTrue(heading.isDisplayed());

        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains("page=" + pageNumber));
    }

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Fix the issue https://github.com/SeleniumHQ/selenium/issues/11750
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get(URL);

        actions = new Actions(driver);
        mainPage = new MainPage(driver);
    }

    @AfterEach
    public void tearDown() {
        driver.quit();
    }

    // @Test
    public void task_1_basic_interacts() throws InterruptedException {
        mainPage.gloceryMenuButton.click();
        waitInSeconds(2);

        WebElement drinksMenu = mainPage.drinksSubMenuButton;
        assertTrue(drinksMenu.isDisplayed());

        // move to Drinks menu
        actions.moveToElement(drinksMenu).perform();
        waitInSeconds(1);
        WebElement drinksJuiceMenu = mainPage.drinksJuiceSubMenuButton;
        assertTrue(drinksJuiceMenu.isDisplayed());

        // select Drinks Juice menu
        drinksJuiceMenu.click();
        waitInSeconds(5);
        WebElement heading = driver.findElement(By.cssSelector("h1[data-testid=\"heading\"]"));
        // Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        // wait.until(d -> heading.isDisplayed());
        assertTrue(heading.isDisplayed());
        assertTrue(heading.getAttribute("innerText").toLowerCase().contains("juice"));

        // navigate to Contact page
        WebElement contact = mainPage.contactLink;
        actions.moveToElement(contact).perform();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> contact.isDisplayed());
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", contact);

        waitInSeconds(5);
        WebElement headerTitle = driver.findElement(By.className("contact-us-page__header__title"));
        wait.until(ExpectedConditions.visibilityOf(headerTitle));
        assertTrue(headerTitle.isDisplayed());
    }

    @Test
    public void task_1_scrapProducts() throws IOException, InterruptedException {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://www.zehrs.ca/food/drinks/juice/c/28230?navid=flyout-L3-Drinks-Juice");
        Thread.sleep(5000);
        // Find product elements
        List<WebElement> products = mainPage.products;
        //waitInSeconds(5000);

        String csvFile = "resources/products_page1.csv";
        CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
        String[] header = {"No", "Product Name", "Price", "Image URL"};
        writer.writeNext(header);

        int count = 1;
        writeProductInfoToCSV(writer, count);

        // Close CSV writer and browser
        writer.close();
        assertFalse(products.isEmpty());
    }

    @Test
    public void task_2_scrapProductsMultiPages() throws IOException, InterruptedException {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://www.zehrs.ca/food/drinks/juice/c/28230?navid=flyout-L3-Drinks-Juice");

        // Find product elements
        List<WebElement> products = mainPage.products;
        //waitInSeconds(5000);

        String csvFile = "resources/products_pages123.csv";
        CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
        String[] header = {"No", "Product Name", "Price", "Image URL"};
        writer.writeNext(header);

        int count = 1;
        // write to file and update count
        count = writeProductInfoToCSV(writer, count);

        // Move to next Page : 2
        int pageNumber = 2;
        switchToNewPage(pageNumber);

        // reload products
        // products = mainPage.products;
        // write to file and update count
        count = writeProductInfoToCSV(writer, count);

        // Move to next Page : 3
        pageNumber = 3;
        switchToNewPage(pageNumber);

        // reload products
        // products = mainPage.products;
        // write to file and update count
        count = writeProductInfoToCSV(writer, count);

        // Close CSV writer and browser
        writer.close();
        assertFalse(products.isEmpty());
    }

    // @Test
    public void task_3_searchProducts() throws InterruptedException {
        String searchStr = "milk & cream";
        mainPage.searchField.sendKeys(searchStr);
        mainPage.searchButton.click();

        waitInSeconds(1);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement searchResult = mainPage.pageTitle;
        wait.until(ExpectedConditions.visibilityOf(searchResult));
        assertTrue(searchResult.isDisplayed());
        assertTrue(searchResult.getAttribute("innerText").toLowerCase().contains(searchStr.toLowerCase()));
    }

    // @Test
    public void task_3_handlePopup() throws InterruptedException {
       // TODO
    }

}
