# variables.tf (in modules/elastic-beanstalk-app)

variable "app_name" {
  description = "The name of the Elastic Beanstalk application."
  type        = string
}

variable "env_name" {
  description = "The name of the Elastic Beanstalk environment."
  type        = string
}

variable "solution_stack_name" {
  description = "The solution stack for the environment, e.g., Corretto 11."
  type        = string
}

variable "instance_type" {
  description = "The EC2 instance type for the environment."
  type        = string
}

variable "version_label" {
  description = "The label for the application version to deploy."
  type        = string
}

variable "s3_bucket_name" {
  description = "The name of the S3 bucket where the application source is stored."
  type        = string
}

variable "source_path" {
  description = "The path to the application source file (.jar or .war)."
  type        = string
}

variable "env_vars" {
  description = "A map of environment variables to be set on the instances."
  type        = map(string)
  default     = {}
}

variable "vpc_id" {
  description = "The ID of the VPC where the resources will be created."
  type        = string
}

variable "public_subnet_ids" {
  description = "A list of public subnet IDs for the environment."
  type        = list(string)
}

variable "private_subnet_ids" {
  description = "A list of private subnet IDs for the environment."
  type        = list(string)
}
