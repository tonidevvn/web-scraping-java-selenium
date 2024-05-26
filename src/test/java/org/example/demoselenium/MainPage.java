package org.example.demoselenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

// page_url = https://www.zehrs.ca/
public class MainPage {
    @FindBy(xpath = "//*[@data-test-marker='Developer Tools']")
    public WebElement seeDeveloperToolsButton;

    @FindBy(xpath = "//*[@data-test='suggestion-action']")
    public WebElement findYourToolsButton;

    @FindBy(xpath = "//button[@data-code=\"xp-455-food-departments\"]")
    public WebElement gloceryMenuButton;

    @FindBy(xpath = "//button[@data-code=\"xp-455-nonfood-departments\"]")
    public WebElement homeBeautyBabyMenuButton;

    @FindBy(xpath = "//button[@data-code=\"xp-455-joe-fresh\"]")
    public WebElement joeFreshMenuButton;

    @FindBy(xpath = "//button[@data-code=\"WhatsNew\"]")
    public WebElement discoverMenuButton;

    @FindBy(css = "a[href$=\"L2-Drinks\"]")
    public WebElement drinksSubMenuButton;

    @FindBy(css = "a[href$=\"L3-Drinks-Juice\"]")
    public WebElement drinksJuiceSubMenuButton;

    @FindBy(css = "a[href$=\"L3-Drinks-Coffee\"]")
    public WebElement drinksCoffeeSubMenuButton;

    @FindBy(css = "input[placeholder=\"Search for product\"]")
    public WebElement searchField;

    @FindBy(css = "input[title=\"Clear Search\"]")
    public WebElement searchClear;

    @FindBy(css = "button[title=\"Submit Search\"]")
    public WebElement searchButton;

    @FindBy(className = "logo--rapid")
    public WebElement rapidLogo;

    @FindBy(className = "page-title__title")
    public WebElement pageTitle;

    @FindBys(@FindBy(css = "div.chakra-linkbox"))
    public List<WebElement> products;

    @FindBy(css = "a[data-track-link-name=\"popular-categories:weekly-flyer\"]")
    public WebElement footerWeeklyFlyer;

    @FindBy(css = "a[data-track-link-name=\"about-us:contact-us\"]")
    public WebElement footerContactUs;

    public MainPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }
}
