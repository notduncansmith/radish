# radish

Super nutrition for regular people

## Overview

Radish is an open-source web application that uses nutritional information about food to help you assemble a diet that meets your needs.

This is not a recipe database. This is not a food log. This is not a diet plan.

Radish gives you the nutritional breakdown of any food in the USDA Food Composition Database, and allows you to compare multiple foods side by side. Additionally, you can build lists of foods to see their aggregate nutritional value.

By using Radish, you will learn what foods to chase down in your daily eating habits. This in turn will give you the confidence to retain full, informed control over what you eat. You don't need a list of pre-approved recipes, and you don't need to sacrifice your time to the calorie-counting gods. All you need to do is see what's in the ingredients you eat, and see what you want to eat in the future.

Recommendations are currently preset for a ~180cm ~68kg adult male. I plan to make these values configurable.

This project currently embeds my USDA.gov API key, which is limited to 1000 requests / ip / hour. Please do not get me in trouble.

## Develop

`lein figwheel`

## License

Copyright © 2017 Duncan Smith

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
