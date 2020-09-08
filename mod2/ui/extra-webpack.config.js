/* 
 *  This is the default license template.
 *  
 *  File: extra-webpack.config.js
 *  Author: ns299g
 *  Copyright (c) 2020 ns299g
 *  
 *  To edit this license information: Press Ctrl+Shift+P and press 'Create new License Template...'.
 */

const webpack = require('webpack')

module.exports = {
    plugins: [
        new webpack.DefinePlugin({
            'process.env': {
                DCAE_HOSTNAME: JSON.stringify(process.env.DCAE_HOSTNAME)
            }
        })
    ]
}