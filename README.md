# Springboot CI/CD devops pipeline 

The repo demonstrates how to create a simple springboot application, build the application using Docker and push the image to ECR and run the application on EKS.


## Features

- Create a simple Springboot application using mongoDB.
- Create a EC2 instance and install Jenkins, Docker.
- Create a ECR repo in AWS. 
- Create a Kubernetes Cluster and worker nodes in Elastic Kubernetes Service(EKS).  
- Deploy the Application to Kubernetes and enable autoscaling. 
- Send logs to Kibana from Kubernetes Pods
- Explore grafana from Elastic Search data.

## Create a simple Springboot application using mongoDB

The Springboot demo application consists of two features.
- `addProduct` will add products to mongoDB
- `findAllProducts` will retrieve all the product details stored in the mongoDB 

Currently, the test case include to check if we get a response for `findAllProducts` from the database.

Install the dependencies and devDependencies and start the server.

## Create a EC2 instance and install Jenkins, Docker.

- Choose an Amazon machine Image(AMI) for this demo I selected (Ubuntu Server 20.04 LTS (HVM), SSD Volume Type - free tier eligible)
- Machine type- t2.micro(free tier eligible).
- Default network was created. 
- Default storage option was selected.
- Connect to the EC2 instance to install the required packages. This information can be found in the instance details.

### Docker Installation

Docker installation steps using default repository from Ubuntu

```
sudo apt-get update
```

Install the below packages
```
sudo apt install gnupg2 pass -y
```
 
Install docker 
```
sudo apt install docker.io -y
```

Add Ubuntu user to Docker group
```
sudo usermod -aG docker $USER
```

The Docker service needs to be setup to run at startup. To do so, type in each command followed by enter:
```
sudo systemctl start docker
sudo systemctl enable docker
sudo systemctl status docker
```
![WhatsApp Image 2021-10-14 at 9 24 56 PM](https://user-images.githubusercontent.com/92055809/137382874-28cac41d-1c91-4fe8-bdd5-449bb7e16faf.jpeg)



### Jenkins Installation

```
sudo apt install openjdk-11-jdk
sudo java -version 
```

```
wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io.key |
sudo apt-key add -
sudo sh -c 'echo deb https://pkg.jenkins.io/debian-stable binary/ > 
/etc/apt/sources.list.d/jenkins.list'
sudo apt-get update
sudo apt-get install jenkins
```

```
sudo systemctl daemon-reload
sudo systemctl start jenkins
sudo systemctl status jenkins
```

Note: I faced an error regarding certificate verification failed. In order to resolve that I used the below command.

```
sudo apt install ca-certificates
sudo apt-get update
```

![WhatsApp Image 2021-10-14 at 9 24 56 PM (1)](https://user-images.githubusercontent.com/92055809/137382925-0c7459d0-be58-47f3-8d5a-327ba9e32fea.jpeg)



## Create a ECR repo in AWS.

![WhatsApp Image 2021-10-14 at 9 24 49 PM](https://user-images.githubusercontent.com/92055809/137382673-575ce891-2810-4a99-a250-be7a81c9bc83.jpeg)
 

- In order to access the ECR from EC2 instance, we need to create a IAM role with `AmazonEC2ContainerRegistryFullAccess` policy and attach/modify the instance with this role.

![WhatsApp Image 2021-10-14 at 9 24 49 PM (1)](https://user-images.githubusercontent.com/92055809/137382787-d6c8de7d-f553-4c5c-8210-c03017ce6eaf.jpeg)


## Create a Kubernetes Cluster and worker nodes in Elastic Kubernetes Service(EKS).

Step 1: Creating an EKS role.

Choose EKS -Cluster and click on permission, automatically “AmazonEKSClusterPolicy” is there for the role.

Step 2: Create a VPC to deploy the cluster.

Go to “AWS CloudFormation” and click on “Create Stack” and give below URL as “Amazon S3 URL”. 

https://amazon-eks.s3-us-west-2.amazonaws.com/cloudformation/2019-02-11/amazon-eks-vpc-sample.yaml


step 3: Create AWS EKS Cluster

Go to the “AWS EKS” service and click “Create cluster”.

step 4: 

kubectl create clusterrolebinding cluster-system-anonymous --clusterrole=cluster-admin --user=system:anonymous

step 5: Configure kubectl for Amazon EKS

Connect to the EC2 instance which was created before and install the below package.

Install kubectl:
https://docs.aws.amazon.com/eks/latest/userguide/install-kubectl.html

Install aws-iam-authenticator : 
https://docs.aws.amazon.com/eks/latest/userguide/install-aws-iam-authenticator.html

Install AWS CLI:
https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html

Once you setup kubectl and AWS-CLI in your machine run below command to configure kubectl for AWS EKS

```
aws eks --region <regionname> update-kubeconfig --name <cluster-name>
```

step 5: IAM Role creation for WorkerNode

Create an IAM policy and use the below policies `AmazonEKSWorkerNodePolicy`, `AmazonEKS_CNI_Policy`, `AmazonEC2ContainerRegistryReadOnly`

step 6: Launching Kubernetes worker nodes

Note: Make sure to select the minimum and maximum size of your nodes for auto scaling.

![WhatsApp Image 2021-10-14 at 9 24 57 PM](https://user-images.githubusercontent.com/92055809/137383175-9671422b-0028-401a-9dff-824289219998.jpeg)


### Deploy the Application to Kubernetes and enable autoscaling.

Step1 : Create a jenkins pipeline 
Step2 : Install docker, docker pipeline,Amazon ECR plugin and configure the Kubernetes configuration.

```
sudo cat ~/.kube/config 
```

Step2 : Below is the pipeline code which was used

```
pipeline {
    agent any
    environment {
        registry = "053757569223.dkr.ecr.us-west-2.amazonaws.com/springboot-application-repo"
    }
    
    stages {
        stage('Cloning Git') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '', url: 'https://github.com/Thejaswininagaraju/springboot-demo-kubernetes']]])      
            }
        }
   
    // Building Docker images
    stage('Building image') {
      steps{
        script {
          dockerImage = docker.build registry
        }
      }
    }
    
    // Uploading Docker images into AWS ECR
    stage('Pushing to ECR') {
     steps{   
         script {
                sh 'aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 053757569223.dkr.ecr.us-west-2.amazonaws.com'
                sh 'docker build -t springboot-application-repo .'
         }
        }
      }
    
         // Stopping Docker containers for cleaner Docker run
     stage('stop previous containers') {
         steps {
            sh 'docker ps -f name=mypythonContainer -q | xargs --no-run-if-empty docker container stop'
            sh 'docker container ls -a -fname=mypythonContainer -q | xargs -r docker container rm'
         }
       }
       
    stage('Create secret') {
     steps{
         script {
                sh 'kubectl apply -f mongo-secret.yml''
            }
      }
    }
     stage('Create PV and PVC') {
     steps{
         script {
                sh 'kubectl apply -f mongo-pv.yml'
                sh 'kubectl apply -f mongo-pvc.yml'
            }
      }
    }

    stage('Create Mongodb') {
     steps{
         script {
                sh 'kubectl apply -f mongo-deployment.yml'
                sh 'kubectl apply -f mongo-service.yml'
            }
      }
    }
       stage('create configmaps') {
     steps{
         script {
                sh 'kubectl apply -f mongo-config.yml'
            }
      }
    }

     stage('Deploy Springboot Application') {
     steps{
         script {
                sh 'kubectl apply -f springboot-deployment.yml'
                sh 'kubectl apply -f springboot-service.yml'

            }
      }
    }
    }
}
```
