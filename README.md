# donetoday

A simple to-done list UI, with support for adding things done today and viewing past days.  

Suggested usage is to add a cronjob (don't forget to have the `MAILTO=postmaster@example.com` set at the top of your crontab), so you can see what you where doing last month.  

And of course add what you are doing today as you go.



### Config 

#### DynamoDB Bootstrapping
This sucker is build on DynamoDB so you'll need an AWS account, with DynamoDB enabled.   Then go to https://console.aws.amazon.com/dynamodb/ and 

1. Add a table added called "donetoday", with a 2 part key 
2. Add a Primary Hash Key:  "user" (type: String)
3. Add a Primary Range Key: "date" (type: Number)

And you are good to go.  Sorry about the muck - someday I'll build this bootstrapping step into the code.

####  Add a ~/.donetoday yaml file like this:
```yaml
secret-key: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
access-key: xxxxxxxxxxxxxxxxxxxx
user: myuniquehandle   # required, but really needed in multi user environments
```

## Usage
<pre>
 Switches               Default     Desc
 --------               -------     ----
 -v, --no-view, --view  false       View things done
 -d, --date             2013-02-09  The date you wish to view
 -h, --no-help, --help  false       Show help
</pre>

### Command line

```bash
> function donetoday {
  # obviously you'll need to alter the path to this repo
    bash -c "cd ~/repos/donetoday/; lein run -- $*"
}

> function donetoday{
  # for more day to day, not hacking. Needs drip, and a run of `lein uberjar`
  drip -jar ~/repos/donetoday/target/donetoday-0.1.0-SNAPSHOT-standalone.jar $*
}

> donetoday -v -d 2013-03-09
I did this 2013-03-09:

* 1st run at donetoday
* Added --view to donetoday
* Added update to donetoday

> donetoday "Updated the readme for donetoday"

```


## License

Copyright © 2013 Nathan McFarland

Distributed under the Eclipse Public License, the same as Clojure.
