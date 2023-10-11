// 覆盖react内置webpack配置的文件
const path = require('path');
const CracoLessPlugin = require('craco-less');

const isProductionENV = process.env.NODE_ENV === 'production';

// 配置生产环境打包不生成.map文件
if (isProductionENV) {
    process.env.GENERATE_SOURCEMAP = false;
}

module.exports = {
    webpack: {
        // 配置路径别名
        alias: {
            src: path.resolve(__dirname, 'src'),
        },
    },
    plugins: [
        {
            // 该插件用于支持less
            plugin: CracoLessPlugin,
        }
    ]
}