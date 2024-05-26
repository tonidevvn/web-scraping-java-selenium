package org.example.demoselenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
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

    @FindBy(css = "a[href$=\"L2-Drinks\"]")
    public WebElement drinksSubMenuButton;

    @FindBy(css = "a[href$=\"L3-Drinks-Juice\"]")
    public WebElement drinksJuiceSubMenuButton;

    @FindBy(css = "input[placeholder=\"Search for product\"]")
    public WebElement searchField;

    @FindBy(css = "input[title=\"Clear Search\"]")
    public WebElement searchClear;

    @FindBy(css = "button[title=\"Submit Search\"]")
    public WebElement searchButton;

    @FindBy(className = "page-title__title")
    public WebElement pageTitle;

    @FindBy(css = "a[data-track-link-name=\"about-us:contact-us\"]")
    public WebElement contactLink;

    @FindBys(@FindBy(css = "div.chakra-linkbox"))
    public List<WebElement> products;

    public MainPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }
}
