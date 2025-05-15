# neighbor-project

This is a spring-boot application that runs on Java, it starts a serverlet locally that runs on port 8080 and accepts a POST request on the API endpoint
"search/spaces"

Once the github repository is cloned locally pls set up the spring-boot application

1 - Navigate to the base directory of the project
2 - Make the setup script executable by running chmod +x .setup.sh
3 - Execute the setup script to setup the environment for running the application by running ./setup.sh
4 - If encountered issues in the build, please follow the logging instructions
5 - Run the application using the mvn script in the project build by running "./mvnw spring-boot:run"
6 - Send POST requests with the initial request to localhost:8080/search/spaces
