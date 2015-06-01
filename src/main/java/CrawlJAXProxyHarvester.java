import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverBackedEmbeddedBrowser;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.plugin.DomChangeNotifierPlugin;
import com.crawljax.core.plugin.HostInterface;
import com.crawljax.core.plugin.HostInterfaceImpl;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.OnUrlLoadPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.inject.Provider;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by csr on 5/28/15.
 */
public class CrawlJAXProxyHarvester {

    static class FirefoxProxyProvider implements Provider<EmbeddedBrowser> {

        private String host;
        private int port;

        public FirefoxProxyProvider(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public EmbeddedBrowser get() {
            String PROXY = host + ":" + port;
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(PROXY).setSslProxy(PROXY).setFtpProxy(PROXY);
            DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability(CapabilityType.PROXY, proxy);
            FirefoxProfile profile = new FirefoxProfile();
            profile.setAcceptUntrustedCertificates(true);
            desiredCapabilities.setCapability(FirefoxDriver.PROFILE, profile );
            WebDriver drivertest = new FirefoxDriver(desiredCapabilities);
            return WebDriverBackedEmbeddedBrowser.withDriver(drivertest);
        }

    }

    public static void main(String[] args) {
//        CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor("https://instagram.com/alternativet_/");
        CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor("https://twitter.com/venstredk");
        builder.setBrowserConfig(new BrowserConfiguration(EmbeddedBrowser.BrowserType.FIREFOX, 1, new FirefoxProxyProvider("localhost", 4338)));
        builder.setMaximumStates(10);
        builder.setMaximumDepth(1);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        final File outputDirectory = new File(System.getProperty("user.home") + "/crawloverview/" + now.format(dateTimeFormatter));
        System.out.println("Archiving in " + outputDirectory.getAbsolutePath());
        HostInterface hostInterface = new HostInterfaceImpl(outputDirectory, null);
        final CrawlOverview crawlOverview = new CrawlOverview(hostInterface);
        builder.addPlugin(crawlOverview);
        builder.addPlugin(new ScrollDownPlugin());
        builder.crawlRules().setInputSpec(new InputSpecification());
        //builder.crawlRules().click("a").withAttribute("class", "mcmPhotoFrontside");
        //builder.crawlRules().click("p").underXPath("//div[@class='content']");
        //builder.crawlRules().clickElementsInRandomOrder(true);
        CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
        crawljax.call();
    }

    public static class ScrollDownPlugin implements OnNewStatePlugin, OnUrlLoadPlugin {

        public void onNewState(CrawlerContext context, StateVertex newState) {
               context.getBrowser().executeJavaScript("window.scrollTo(0,document.body.scrollHeight);");
        }

        public void onUrlLoad(CrawlerContext context) {
            context.getBrowser().executeJavaScript("window.scrollTo(0,document.body.scrollHeight);");
            context.getBrowser().executeJavaScript("document.body.initEvent('click', 'true', 'true');");
        }


    }

}
