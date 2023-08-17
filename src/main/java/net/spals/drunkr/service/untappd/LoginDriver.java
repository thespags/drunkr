package net.spals.drunkr.service.untappd;

import javax.validation.constraints.NotNull;

import com.google.inject.Inject;

import com.netflix.governator.annotations.Configuration;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver for logging into untappd using a user's credentials.
 *
 * @author spags
 */
class LoginDriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginDriver.class);
    private static final By USERNAME_LOGIN_PATH = By.id("username");
    private static final By PASSWORD_LOGIN_PATH = By.id("password");
    @NotNull
    @Configuration("untappd.master.user")
    private String untappdMasterUser;
    @NotNull
    @Configuration("untappd.master.password")
    private String untappdMasterPassword;

    @Inject
    LoginDriver() {
    }

    void setConfiguration(final String untappdMasterUser, final String untappdMasterPassword) {
        this.untappdMasterUser = untappdMasterUser;
        this.untappdMasterPassword = untappdMasterPassword;
    }

    void login(final WebDriver driver) {
        LOGGER.info("attempting to login");

        try {
            final WebDriverWait wait = new WebDriverWait(driver, 5);
            wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_LOGIN_PATH));
        } catch (final Throwable x) {
            LOGGER.info("failed to log in", x);
            return;
        }

        final WebElement username = driver.findElement(USERNAME_LOGIN_PATH);
        username.sendKeys(untappdMasterUser);

        final WebElement password = driver.findElement(PASSWORD_LOGIN_PATH);
        password.sendKeys(untappdMasterPassword);
        password.submit();
        LOGGER.info("successfully logged in");
    }
}
