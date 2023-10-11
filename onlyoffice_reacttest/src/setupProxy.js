const { createProxyMiddleware: proxy } = require('http-proxy-middleware');
module.exports = function (app) {
    app.use(
        proxy('/test', {
            target: 'http://127.0.0.1:18181',
            secure: false,//是否验证htpps的安全证书，如果域名是https需要配置此项
            changeOrigin: true,//必须设置为true
        })
    );
};