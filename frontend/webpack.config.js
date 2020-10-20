const path = require('path')
const getWebpackConfig = require('@jetbrains/teamcity-api/getWebpackConfig')

module.exports = getWebpackConfig({
    srcPath: path.join(__dirname, './src'),
    outputPath: path.resolve(__dirname, '../slackIntegration-server/src/main/resources/buildServerResources'),
    entry: './src/index.ts',
    useTypeScript: true,
})