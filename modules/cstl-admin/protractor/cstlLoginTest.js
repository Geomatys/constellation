// myTest.js
var ptor = protractor.getInstance();
//ptor.sleep(5000);
ptor.ignoreSynchronization = true;

describe('cstl admin homepage', function() {
  it('should log the named user', function() {
      ptor.get('http://localhost:8080/cstl-admin/');
      var elem = element(By.id('authenticateLink'));
      elem.click().then(function(newUrl) {
          element(By.id('username')).sendKeys('admin');
          element(By.id('password')).sendKeys('admin');
          loginBtn = ptor.findElement(protractor.By.tagName('button'))
          loginBtn.click().then( function(){
              ptor.getCurrentUrl().then(function(url) {
                  expect(url).toBe('http://localhost:8080/cstl-admin/');
                  var buttonWS = ptor.findElement(protractor.By.id('buttonWS'));
                  buttonWS.click().then(function(){
                      var testTitle = ptor.findElement(protractor.By.className('navbar-brand'));
                      console.log('-'+testTitle.getText()+'-');
                      var firstWMS =  ptor.findElement(
                          protractor.By.repeater('service in services.instance').
                              row(0).column('{{service.name}}'));
                      expect(firstWMS.getText()).toEqual('test1 (WMS)');
                  });
              });
          });
      });
  });
});
