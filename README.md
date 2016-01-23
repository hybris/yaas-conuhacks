# About Wishlist in yaas-conuhacks

The following path is to understand the YaaS platform. When you are done for the wishlist service, you can continue creating a new service for your problem domain.

- Register - https://www.yaas.io/register
- Setup your system with prerequisites - https://devportal.yaas.io/gettingstarted/prerequisites/index.html
- Create a microservice - https://devportal.yaas.io/gettingstarted/createaservice/index.html
- Create Organization, Project, Application in builder - https://devportal.yaas.io/gettingstarted/setupaproject/index.html
- Understand the code and Populate those settings in the default.properties
- Integrate the Repository client and Resource
- Test your service

## Next Steps

Now that you understand how the service creation works. Go hack and create your own services and packages.

This repository contains the following folders

- postman - Contains postman collections that you can use with your YaaS project and application
- resources 
    - default.properties - Populate the details of your project, application, clientId and clientSecret of your application.
- restclient
    - RepositoryClient - It is a rest client that works with the Repository service of Persistence Package.
- wishlist
    - DefaultWishlistsResource - Implementation of the resource class.

## References
- Reference Implementation - https://github.com/SAP/yaas_java_jersey_wishlist





