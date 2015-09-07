Decimal Histogram Facet Plugin for elasticsearch
================================================

This plugin provides a facet for [elasticsearch](http://www.elasticsearch.org/) that works like the built-in histogram facet, but supports floating point intervals as well as offsets.

To install the plugin, run:

```
bin/plugin --url https://github.com/zenobase/decimal-histogram-facet/releases/download/0.0.4/decimal-histogram-facet-0.0.4.jar --install decimal-histogram-facet
```


Versions
--------

<table>
  <thead>
    <tr>
      <th>decimal-histogram-facet</th>
      <th>elasticsearch</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>
        0.0.5 -> master
        <a href="https://travis-ci.org/zenobase/decimal-histogram-facet"><img title="Build Status" src="https://travis-ci.org/zenobase/decimal-histogram-facet.png?branch=master"/></a>
      </td>
      <td>1.4.x -> 1.7.x</td>
    </tr>
    <tr>
      <td>0.0.4</td>
      <td>1.2.x, 1.3.x</td>
    </tr>
    <tr>
      <td>0.0.3</td>
      <td>1.0.x, 1.1.x</td>
    </tr>
    <tr>
      <td>0.0.2</td>
      <td>0.90.6, 0.90.7</td>
    </tr>
    <tr>
      <td>0.0.1</td>
      <td>0.90.5</td>
    </tr>
  </tbody>
</table>


Parameters
----------

<table>
  <tbody>
    <tr>
      <th>field</th>
      <td>The name of a field containing numeric values.</td>
    </tr>
    <tr>
      <th>interval</th>
      <td>The (floating point) bucket size.</td>
    </tr>
    <tr>
      <th>offset</th>
      <td>Optional (floating point) offset for each bucket.</td>
    </tr>
  </tbody>
</table>


Example
-------

Histogram with 0.5 Celsius degree buckets on values that are stored in degrees Kelvin:

Query:

```javascript
{
    "query" : { ... }
    "facets" : {
        "places" : { 
            "decimal_histogram" : {
                "field" : "temperature",
                "interval" : 0.5,
                "offset" : -273.15
            }
        }
    }
}
```

```java
SearchSourceBuilder search = ...
search.facet(new DecimalHistogramFacetBuilder("demo", "temperature", 0.5, -273.15, HistogramFacet.ComparatorType.KEY));
```

Result:

```javascript
{
    ...
    "facets" : {
        "demo" : {
            "entries" : [ {
              "key" : 20.0
              "count" : 1
            }, {
              "key" : 20.5
              "count" : 3
        } ]
    }
}
```


License
-------

```
This software is licensed under the Apache 2 license, quoted below.

Copyright 2012-2014 Zenobase LLC

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
