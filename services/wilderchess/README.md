# Wilderchess: ML-Driven Strategy Engine

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk) ![Python](https://img.shields.io/badge/Python-3.12-blue?logo=python) ![AWS](https://img.shields.io/badge/AWS-Cloud-232F3E?logo=amazon-aws) ![Terraform](https://img.shields.io/badge/Terraform-1.x-623CE4?logo=terraform)

A high-concurrency game engine and infrastructure platform designed for real-time PvP and large-scale Reinforcement Learning data generation.

## 🏗 System Architecture
* **Backend:** Java 21 (Amazon Corretto) with high-availability WebSockets.
* **Infrastructure:** Modular Terraform managing AWS Elastic Beanstalk, ECR, and Spot Instance Fleets.
* **Inference:** Integrated ONNX Runtime for sub-1ms model execution.

## 📊 Performance & Insights
| Inference Latency | Strategy Heatmap |
| :---: | :---: |
| ![Latency](docs/images/inference_latency.png) | ![Heatmap](docs/images/strategy_heatmap.png) |

---

## 📂 Project Structure
```text
wilderchess/
├── src/wilderchess/     # Core Library (Python)
├── src/main/java/       # Game Engine (Java)
├── ops/                 # Shell Deployment & CI/CD Scripts
├── scripts/             # ML Utility & Data Analysis Scripts
├── models/              # ONNX Binaries & Scalers
├── docs/images/         # Performance Charts & Heatmaps
├── pom.xml              # Maven Configuration
└── pyproject.toml       # UV / Python Configuration

---

## 💻 Local Development (DX)
This project utilizes a high-performance **VS Code Dev Container** environment. 

1. **Environment:** Open the project in VS Code and select "Reopen in Container".
2. **Package Management:** Run `uv sync` to initialize the Python/ML environment via hard-links (optimized for speed and disk space).
3. **Execution:** Use the operations script to launch the local engine:

```bash
./ops/run.sh

🚀 AWS Cloud Deployment
The production environment is hosted on AWS Elastic Beanstalk with automated deployment scripts.

### 🔐 Authentication & Environment
This project uses separate AWS accounts for Simulation (Dev) and Production (EB).

**1. Authenticate via AWS SSO:**
```bash
aws sso login --profile dev
aws sso login --profile prod

2. Target the Simulation Environment (Bot Generation):

Bash
export AWS_PROFILE=dev
./ops/push_bot.sh
3. Target the Production Environment (Elastic Beanstalk):

Bash
export AWS_PROFILE=prod
./ops/deploy.sh

Configuration: Update the application environment variables or endpoint configuration to point to the production Elastic Beanstalk URL.

Execution: Deploy a specific version to the environment:

Bash
./ops/deploy.sh {version_tag}
🤖 Bot Orchestration & ML Training
This platform utilizes AWS Spot Fleets to generate large-scale game-state datasets for machine learning training.

1. Update Simulation Artifacts
Push the latest bot logic/image to Amazon ECR:

Bash
./ops/push_bot
2. Infrastructure Scaling
Modify main.tf to configure the simulation parameters:

Scale Capacity: Adjust desired_bots (e.g., 1 for testing, 10+ for data generation).

Logic Switches:

Set runbotvsbot to true to initiate autonomous simulations.

Set botIsAi to false for rule-based heuristic generation, or true for Reinforcement Learning training.

3. Provision Infrastructure
Bash
terraform plan
terraform apply
🛠 Project Highlights
Cost Optimization: Leveraged AWS Spot Instances via Auto Scaling Group (ASG) Mixed Instance Policies, reducing data generation costs by ~80%.

Zero-Trust Identity: Utilizes GitHub Actions via OIDC for secure, keyless cloud deployments.

Infrastructure-as-Code: 100% of the cloud stack is defined and managed via Terraform for repeatable deployments.