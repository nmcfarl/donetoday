# donetoday

A simple to-done list UI, with support for adding things done toady and viewing past days.  

Suggested usage is to add a cronjob (don't forget to have the `MAILTO=postmaster@example.com` set at the top of your crontab), so you can see what you where doing last month.  

And of course add what you are doing today as you go.



### Config 

This sucker is build on DynamoDB so you'll need an AWS account, with DynamoDB enabled.  And a table added called "donetoday", 2 part key "user" string, "date" number (range).  Sorry - someday I'll build this bootstrapping step into the code.

#### ~/.donetoday
secret-key: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
access-key: xxxxxxxxxxxxxxxxxxxx
user: myuniquehandle   # required, but really needed in multi user environments


## Usage

 Switches               Default     Desc
 --------               -------     ----
 -v, --no-view, --view  false       View things done
 -d, --date             2013-02-09  The date you wish to view
 -h, --no-help, --help  false       Show help


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
