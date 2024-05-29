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

/**
 * MainPageTest class containing test cases for the MainPage class.
 * This class uses JUnit for testing and Selenium WebDriver for browser automation.
 */
public class MainPageTest {
    // WebDriver instance for interacting with the web browser
    private WebDriver driver;

    // MainPage instance for accessing web elements on the main page
    private MainPage mainPage;

    // JavascriptExecutor instance for executing JavaScript commands
    JavascriptExecutor js;

    // Actions instance for performing complex user interactions
    Actions actions;

    // URL of the website to be tested
    private static final String URL = "https://www.zehrs.ca/";

    // Maximum number of products to be extracted per page
    private static final int MAX_PER_PAGE = 5;

    /**
     * Method to wait for a specified number of seconds.
     * Uses implicit wait to pause the test execution.
     *
     * @param seconds Number of seconds to wait.
     */
    private void waitInSeconds(int seconds) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(seconds));
    }

    /**
     * Method to click a web element using JavaScript.
     * Waits for the element to be displayed before clicking it.
     *
     * @param element The web element to be clicked.
     */
    private void clickByJs(WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> element.isDisplayed());
        if (js != null) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    /**
     * Method to extract product information from a web element.
     * Retrieves the product name and price.
     *
     * @param projectNode The web element representing a product.
     * @return An array containing the product name, price, image.
     */
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

    /**
     * Writes product information to a CSV file.
     *
     * @param writer The CSVWriter instance used to write data to the CSV file.
     * @param startIndex The starting index for the product count.
     * @return The next index after the last written product.
     * @throws IOException If an I/O error occurs while writing to the CSV file.
     */
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

    /**
     * Switches to a new page based on the provided page number.
     *
     * @param pageNumber The number of the page to switch to.
     */
    private void switchToNewPage(int pageNumber) {
        WebElement nextPage = driver.findElement(By.cssSelector("a[aria-label=\"Page " + pageNumber + "\"]"));
        actions.moveToElement(nextPage).perform();
        waitInSeconds(5);
        clickByJs(nextPage);
        waitInSeconds(5);

        // checkpoint: heading
        WebElement heading = driver.findElement(By.cssSelector("h1[data-testid=\"heading\"]"));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOf(heading));
        assertTrue(heading.isDisplayed());

        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains("page=" + pageNumber));
    }

    /**
     * Checks if the page heading matches the expected heading text.
     *
     * @param heading The expected heading text.
     * @return true if the heading matches the expected text, false otherwise.
     */
    private boolean pageHeadingCheck(String heading) {
        WebElement h1 = driver.findElement(By.cssSelector("h1[data-testid=\"heading\"]"));
        assertTrue(h1.isDisplayed());
        return h1.getAttribute("innerText").toLowerCase().contains(heading.toLowerCase());
    }

    /**
     * Sorts the products by a given index.
     *
     * @param select_index The index to select the sort option.
     */
    private void sortByProducts(int select_index) {
        // change Sort By
        WebElement sortBy = driver.findElement(By.cssSelector("button[aria-labelledby=\"sort-by menu-button-:r1:\"]"));
        actions.moveToElement(sortBy);
        waitInSeconds(5);
        sortBy.click();
        waitInSeconds(5);

        WebElement sortBySelection = driver.findElement(By.cssSelector("button[data-testid=\"menu-item\"][data-index=\"" + select_index +"\"]"));
        actions.moveToElement(sortBySelection);
        waitInSeconds(5);
        sortBySelection.click();
        waitInSeconds(5);
    }

    /**
     * Setup method to initialize the WebDriver, JavascriptExecutor, and Actions.
     * This method runs before each test.
     */
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
        js = (JavascriptExecutor) driver;
    }

    /**
     * Tear down method to close the WebDriver.
     * This method runs after each test.
     */
    @AfterEach
    public void tearDown() {
        driver.quit();
    }

    /**
     * Test case to interact with the menu and verify visibility of various submenus.
     *
     * @throws InterruptedException If thread sleep is interrupted.
     */
    @Test
    public void task_1_1_menu_interact() throws InterruptedException {
        mainPage.gloceryMenuButton.click();
        waitInSeconds(2);

        WebElement drinksMenu = mainPage.drinksSubMenuButton;
        assertTrue(drinksMenu.isDisplayed());

        // move to Drinks menu
        actions.moveToElement(drinksMenu).perform();
        waitInSeconds(5);

        // move to Drinks >> Juice from submenu bar
        WebElement drinksJuiceMenu = mainPage.drinksJuiceSubMenuButton;
        assertTrue(drinksJuiceMenu.isDisplayed());
        actions.moveToElement(drinksJuiceMenu).perform();
        waitInSeconds(5);

        // move to Drinks >> Coffee from submenu bar
        WebElement drinksCoffeeMenu = mainPage.drinksCoffeeSubMenuButton;
        assertTrue(drinksCoffeeMenu.isDisplayed());
        actions.moveToElement(drinksCoffeeMenu).perform();
        waitInSeconds(5);

        // move to Home Beauty & Baby menu
        WebElement homeBeautyBabyMenu = mainPage.homeBeautyBabyMenuButton;
        assertTrue(homeBeautyBabyMenu.isDisplayed());
        actions.moveToElement(homeBeautyBabyMenu).perform();
        waitInSeconds(5);

        // move to JoeFresh menu
        WebElement joeFreshMenu = mainPage.joeFreshMenuButton;
        assertTrue(joeFreshMenu.isDisplayed());
        actions.moveToElement(joeFreshMenu).perform();
        waitInSeconds(5);

        // move to Discover menu
        WebElement discoverMenu = mainPage.discoverMenuButton;
        assertTrue(discoverMenu.isDisplayed());
        actions.moveToElement(discoverMenu).perform();
        waitInSeconds(5);
    }

    /**
     * Test case to interact with the product page and apply various filters.
     *
     * @throws InterruptedException If thread sleep is interrupted.
     */
    @Test
    public void task_1_2_product_page_interact() throws InterruptedException {
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
        assertTrue(pageHeadingCheck("juice"));

        // select Price Low to High
        sortByProducts(1);

        // select Price High To Low
        sortByProducts(2);

        // select brand filter of juice products
        WebElement brandFilter = driver.findElement(By.cssSelector("input[name=\"Sunny Delight\"]"));
        actions.moveToElement(brandFilter);
        waitInSeconds(5);
        clickByJs(brandFilter);

        // wait for page reload
        waitInSeconds(5);
        String newUrl = driver.getCurrentUrl();
        // checkpoint URL's parameter
        assertTrue(newUrl.contains("productBrand=SUND"));
    }

    /**
     * Test case to interact with footer links and verify navigation.
     *
     * @throws InterruptedException If thread sleep is interrupted.
     */
    @Test
    public void task_1_3_footer_interact() throws InterruptedException {
        // navigate to Weekly flyer
        WebElement weeklyFlyer = mainPage.footerWeeklyFlyer;
        actions.moveToElement(weeklyFlyer).perform();
        waitInSeconds(5);
        clickByJs(weeklyFlyer);
        waitInSeconds(5);
        assertTrue(pageHeadingCheck("flyer items"));

        // navigate to Contact page
        WebElement contact = mainPage.footerContactUs;
        actions.moveToElement(contact).perform();
        waitInSeconds(5);
        clickByJs(contact);

        waitInSeconds(5);
        WebElement headerTitle = driver.findElement(By.className("contact-us-page__header__title"));
        assertTrue(headerTitle.isDisplayed());
    }

    /**
     * Test case to scrape product information from a single page and save to a CSV file.
     *
     * @throws IOException If an I/O error occurs while writing to the CSV file.
     */
    @Test
    public void task_1_4_scrapProducts() throws IOException {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://www.zehrs.ca/food/drinks/juice/c/28230?navid=flyout-L3-Drinks-Juice");
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

    /**
     * Test case to scrape products from multiple pages and save to a CSV file.
     *
     * @throws IOException If an I/O error occurs while writing to the CSV file.
     */
    @Test
    public void task_2_1_scrapProductsMultiPages() throws IOException {
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

    /**
     * Test case to scrape products from different categories and save to a CSV file.
     *
     * @throws IOException If an I/O error occurs while writing to the CSV file.
     */
    @Test
    public void task_2_2_scrapProductsDifferentPages() throws IOException {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://www.zehrs.ca/food/drinks/juice/c/28230?navid=flyout-L3-Drinks-Juice");

        // Find product elements
        List<WebElement> products = mainPage.products;
        //waitInSeconds(5000);

        String csvFile = "resources/products_dif_cat.csv";
        CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
        String[] header = {"No", "Product Name", "Price", "Image URL"};
        writer.writeNext(header);

        int count = 1;
        // write to file and update count
        count = writeProductInfoToCSV(writer, count);

        // Change to Drinks Coffee page
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://www.zehrs.ca/food/drinks/coffee/c/28228?navid=flyout-L3-Drinks-Coffee");

        // reload products
        products = mainPage.products;
        // write to file and update count
        writeProductInfoToCSV(writer, count);

        // Close CSV writer and browser
        writer.close();
        assertFalse(products.isEmpty());
    }

    /**
     * Test method for searching a product.
     * Verifies that the search result contains the searched text.
     *
     * @throws InterruptedException If the thread is interrupted during sleep.
     */
    @Test
    public void task_3_1_searchProducts() throws InterruptedException {
        String searchStr = "milk & cream";
        mainPage.searchField.sendKeys(searchStr);
        mainPage.searchButton.click();

        waitInSeconds(5);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement searchResult = mainPage.pageTitle;
        wait.until(ExpectedConditions.visibilityOf(searchResult));
        assertTrue(searchResult.isDisplayed());
        assertTrue(searchResult.getAttribute("innerText").toLowerCase().contains(searchStr.toLowerCase()));

        waitInSeconds(5);
        mainPage.searchClear.click();
        waitInSeconds(5);
        searchStr = "coffee";
        mainPage.searchField.sendKeys(searchStr);
        mainPage.searchButton.click();
        waitInSeconds(5);
        wait.until(ExpectedConditions.visibilityOf(searchResult));
        assertTrue(searchResult.isDisplayed());
        assertTrue(searchResult.getAttribute("innerText").toLowerCase().contains(searchStr.toLowerCase()));
    }

    /**
     * Test method for handling a popup and verifying certain actions on the main page.
     * Verifies elements like the rapid logo, address autocomplete, continue button, and service availability.
     *
     * @throws InterruptedException If the thread is interrupted during sleep.
     */
    @Test
    public void task_3_2_handlePopup() throws InterruptedException {
        WebElement rapidLogo = mainPage.rapidLogo;
        clickByJs(rapidLogo);
        waitInSeconds(5);

        // get autocomplete text box
        WebElement autoComplete = driver.findElement(By.cssSelector("input[id=\"addressAutocomplete\"]"));
        autoComplete.sendKeys("401 Sunset Ave");
        waitInSeconds(5);

        // pick first item in the complete list
        WebElement itemOne = driver.findElement((By.cssSelector(".address-autocomplete__address-list li:nth-child(1)")));
        clickByJs(itemOne);
        waitInSeconds(10);

        // check point 1 =>> continue
        WebElement buttonContinue = driver.findElement(By.cssSelector("button.address-autocomplete-modal__button-continue"));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(buttonContinue));
        assertTrue(buttonContinue.isEnabled());
        buttonContinue.click();
        waitInSeconds(10);

        // check point 2 =>> no service available
        WebElement searchResult = driver.findElement(By.cssSelector("h1.no-serviceability__title"));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String msg = "We’re sorry but it looks like we’re not available in your area yet.";
        wait.until(ExpectedConditions.textToBePresentInElement(searchResult, msg));
        assertTrue(searchResult.isDisplayed());
        assertTrue(searchResult.getAttribute("innerText").toLowerCase().contains(msg.toLowerCase()));

        // check point 3 =>> return to main page
        WebElement returnToZehrs = driver.findElement(By.cssSelector("a.no-serviceability__go-back-button"));
        returnToZehrs.click();
        waitInSeconds(5);
    }

}
