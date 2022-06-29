创建 ES 索引 index_es

注：为了实现多租户，需要创建大于1的 number_of_shards

```
PUT /index_es
{
  "settings": {
    "number_of_shards": 2
  }, 
  "mappings": {
    "properties": { 
  		"platformId": {
  			"type" : "text",
  			"fields" : {
  				"keyword" : {
  				"type" : "keyword",
  				"ignore_above" : 256
  				}
  			}
  		},
  		"dataId" : {
  			"type" : "text",
  			"fields" : {
  				"keyword" : {
  				"type" : "keyword",
  				"ignore_above" : 256
  				}
  			}
  		},
  		"source" : {
  			"type" : "text",
  			"fields" : {
  				"keyword" : {
  				"type" : "keyword",
  				"ignore_above" : 256
  				}
  			}
  		},
  		"url" : {
  			"type" : "text",
  			"fields" : {
  				"keyword" : {
  				"type" : "keyword",
  				"ignore_above" : 256
  				}
  			}
  		},
  		"createTime" : {
  			"type" : "text",
  			"fields" : {
  				"keyword" : {
  				"type" : "keyword",
  				"ignore_above" : 256
  				}
  			}
  		},
  		"issuedTime" : {
  			"type" : "text",
  			"fields" : {
  				"keyword" : {
  				"type" : "keyword",
  				"ignore_above" : 256
  				}
  			}
  		},
  		"labelText": {
  			"type": "text",
  			"analyzer": "ik_max_word",
  			"search_analyzer": "ik_smart"
  		},	
  		"contentText": {
  			"type": "text",
  			"analyzer": "ik_max_word",
  			"search_analyzer": "ik_smart"
  		},
  		"titleText": {
  			"type": "text",
  			"analyzer": "ik_max_word",
  			"search_analyzer": "ik_smart"
  		},			
  		"summaryText": {
  			"type": "text",
  			"analyzer": "ik_max_word",
  			"search_analyzer": "ik_smart"
  		},
  		"hotspot": {
  			"type": "double"
  		}
  		
  		
    }
  }
}
```

