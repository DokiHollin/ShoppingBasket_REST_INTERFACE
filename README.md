# Assignment 3: REST

## Note about the submission:
Because I'm writting under the windows system, the question 5 to 8 need to implement the special REST command 
in order for the windows cmd to work.
#### Original: curl -s -H "Content-Type: application/json" -X POST -d '{"name":"papaya", "cost":"1.0"}' http://localhost:8080/api/shop/costs | jq .
#### Now:  curl -s -H "Content-Type: application/json" -X POST -d "{\"name\":\"papaya\", \"cost\":\"1.0\"}" http://localhost:8080/api/shop/costs | jq .
#### So I need to add backslash, if the MakeFile for Q5-8 doesn't work, please uncomment the backslashed version which I already stated
#### inside of the Makefile please! Sorry for any inconvenience :))
