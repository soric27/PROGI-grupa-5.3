const puppeteer = require('puppeteer');

(async () => {
  const browser = await puppeteer.launch({ args: ['--no-sandbox', '--disable-setuid-sandbox'] });
  const page = await browser.newPage();

  page.on('console', async (msg) => {
    try {
      const vals = await Promise.all(msg.args().map(a => a.jsonValue()));
      console.log('PAGE_CONSOLE', msg.type(), ...vals);
    } catch (e) {
      console.log('PAGE_CONSOLE', msg.type(), msg.text());
    }
  });

  try {
    await page.goto('http://localhost:3000', { waitUntil: 'networkidle2', timeout: 15000 });
    // wait for any logs
    await page.waitForTimeout(1500);
  } catch (e) {
    console.error('NAV_ERROR', e.message);
  }

  await browser.close();
})();