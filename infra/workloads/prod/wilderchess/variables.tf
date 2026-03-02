variable "prod_instance_type" {
  description = "The EC2 instance type to use for the production Elastic Beanstalk environment."
  type        = string
  default     = "t2.micro"
}

variable "app_version" {
  description = "The version label for the application to be deployed."
  type        = string
}
