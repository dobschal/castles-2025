// eslint-disable-next-line @typescript-eslint/no-var-requires
process.env.VUE_APP_VERSION = require("./package.json").version;
const NodePolyfillPlugin = require("node-polyfill-webpack-plugin");

module.exports = {
  chainWebpack: (config) => {
    config.plugin("polyfills").use(NodePolyfillPlugin);
  },
  devServer: {
    port: 54422,
  },
  configureWebpack: {
    module: {
      rules: [
        {
          test: /\.md$/i,
          loader: "raw-loader",
        },
      ],
    },
  },
  productionSourceMap: false,
  lintOnSave: false, //This stops the build (gitlab pipeline) step from running lint. So the linting can happen in the respective step after the build.
};
