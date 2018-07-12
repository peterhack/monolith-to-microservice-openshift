var casper = require('casper').create({
    verbose: true,
    // logLevel: "debug",
    pageSettings: {
        webSecurityEnabled: false,
        loadImages:  false, 
        loadPlugins: true
    }
});

var x = require('casper').selectXPath;

casper.on("page.error", function(msg, trace) {
    console.log(msg);
    console.log('More info: ' + JSON.stringify(trace, null, 4));
});

casper.start();
casper.viewport(1200, 100);

if (casper.cli.args.length === 0 && Object.keys(casper.cli.options).length === 0) {
    this.echo("Usage: sample.js url").exit(1);
}

if( casper.cli.args[1] === 'ch') {
    // Chrome Agent
    casper.userAgent('Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36');
} else {
    // Firefox Agent
    casper.userAgent('Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/604.5.6 (KHTML, like Gecko) Version/11.0.3 Safari/604.5.6');
}

if( casper.cli.args[2] === 0 ) { // 10 percent get the Cbr-header for the Dark Launch injected
    casper.page.customHeaders = { "Cbr-Header": "ui-v1" };
} 

casper.thenOpen(casper.cli.args[0], function(){
    this.echo("Title: "+this.getTitle());
});

casper.waitForSelector(x('//*[@id="navbar-items"]/ul/li[3]/a'), null, null, 15000)
    .then(function(){
        // this.capture('screen_home.png');
        this.click(x('//*[@id="navbar-items"]/ul/li[3]/a'));
        this.echo("Event link clicked");
    }
);

var i = 0;
do {
    
    casper.waitForSelector(x('//*[@id="eventCarousel"]/div/div[2]/div/div/div[2]'), null, null, 15000)
        .then(function(){
            // this.capture('screen_event.png');
            this.click(x('//*[@id="navbar-items"]/ul/li[5]/a'));
            this.echo("Venue link clicked");
        }
    );

    casper.waitForSelector(x('//*[@id="venueCarousel"]/div/div[3]/div/div/div[2]'), null, null, 15000)
        .then(function(){
            //this.capture('screen_venue.png');
            this.click(x('//*[@id="navbar-items"]/ul/li[7]/a'));
            this.echo("Booking link clicked");
        }
    );

    casper.waitForSelector(x('//*[@id="content"]/div/div[2]/div/ul/li[3]'), null, null, 15000)
        .then(function(){
            // this.capture('screen_bookings.png');
            this.click(x('//*[@id="navbar-items"]/ul/li[3]/a'));
            this.echo("Event link clicked");
        }
    );

    i++;

} while (i <= 3);


casper.run();