# Phone Directory  

### How to run  
Once the repository is cloned:
1. Move `interview-callerid-data.csv` to the `resources` directory.  
2. Set the port. In `resources/config.edn` you will see `{ :port 8080 }`. Change the port if you would like.  
3. In the project directory, run  
```
lein run
```

Here are sample curl commands:  
**GET Request:**
```
curl http://localhost:8080/query?number=%2B17193346351
```
**POST Request:**
```
curl -H 'Content-Type: application/json' -d '{"body": {"name": "YourName", "context": "YourContext", "number": "+12345678910"}}' -X POST http://localhost:8080/number
```
### Stack  
Originally, I had plans to build a small front end to make it easier to launch requests. Unfortunately, I didn't quite get there. For small front to back Clojure(Script) web apps, I have been using this Leiningen [template](https://github.com/gered/simple-web-app-template) and started tinkering with that. This template, uses ring, compojure, and immutant on the back, and figwheel and reagent on the front.  

I used *Clojure.data.csv* to read from the csv, *Clojure.data.json* for `edn/json` conversions, *Clojure.edn* for reading from the config, and *midje* for testing.  

### Thins to improve on
