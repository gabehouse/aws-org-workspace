vpc_cidr        = "10.1.0.0/16" # Changed '0' to '1'
# Distinct ranges that match the 10.1.x.x block
public_subnets  = ["10.1.1.128/26", "10.1.1.192/26"]
intra_subnets   = ["10.1.1.0/26", "10.1.1.64/26"]