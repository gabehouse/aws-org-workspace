import React, { useRef, useState, useEffect } from 'react';
import ColorGrid from './ColorGrid';
import Markdown from 'react-markdown';
import './ProjectCards.css';
import { GithubLogo } from "@phosphor-icons/react/dist/icons/GithubLogo";
import { LinkedinLogo } from "@phosphor-icons/react/dist/icons/LinkedinLogo";
import { Envelope } from "@phosphor-icons/react/dist/icons/Envelope";
import { X } from "@phosphor-icons/react/dist/icons/X";
import { Gear } from "@phosphor-icons/react/dist/icons/Gear";
import { Books } from "@phosphor-icons/react/dist/icons/Books"

const App = () => {
    const homeRef = useRef(null);
    const projectsRef = useRef(null);
    const contactRef = useRef(null);
    const [currentPage, setCurrentPage] = useState("home");
    const [dimensions, setDimensions] = useState({
        width: document.documentElement.clientWidth,
        height: document.documentElement.clientHeight
    });

    // Update dimensions on window resize
    useEffect(() => {
        const handleResize = () => {
            setDimensions({
                width: document.documentElement.clientWidth,
                height: document.documentElement.clientHeight
            });
            console.log(document.documentElement.clientWidth);
        };

        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, []);

    // Scroll observer to update current page
    useEffect(() => {
        const observer = new IntersectionObserver(
            (entries) => {
                entries.forEach(entry => {
                    // Check isIntersecting AND use a slightly more lenient logic
                    if (entry.isIntersecting) {
                        if (entry.target === homeRef.current) setCurrentPage("home");
                        else if (entry.target === projectsRef.current) setCurrentPage("projects");
                        else if (entry.target === contactRef.current) setCurrentPage("contact");
                    }
                });
            },
            {
                // rootMargin: top, right, bottom, left
                // This shrinks the "checking area" to the top 20% of the viewport
                rootMargin: '-20% 0% -70% 0%',
                threshold: 0
            }
        );

        if (homeRef.current) observer.observe(homeRef.current);
        if (projectsRef.current) observer.observe(projectsRef.current);
        if (contactRef.current) observer.observe(contactRef.current);

        return () => {
            if (homeRef.current) observer.unobserve(homeRef.current);
            if (projectsRef.current) observer.unobserve(projectsRef.current);
            if (contactRef.current) observer.unobserve(contactRef.current);
        };
    }, []);

    // 1. Add this helper function inside your App component
    const handleScroll = (ref) => {
        if (!ref.current) return;

        // "The Kill Switch": briefly locking overflow stops the
        // native browser smooth-scroll engine instantly.
        document.documentElement.style.scrollBehavior = 'auto';

        // Force a tiny layout shift to ensure the browser registers the stop
        window.scrollTo({
            top: window.pageYOffset,
            behavior: 'auto'
        });

        // Re-enable smooth behavior and trigger the new scroll
        requestAnimationFrame(() => {
            ref.current.scrollIntoView({ behavior: 'smooth' });
        });
    };

    // 2. Update your button handlers to use the helper
    const scrollToHome = () => handleScroll(homeRef);
    const scrollToProjects = () => handleScroll(projectsRef);
    const scrollToContact = () => handleScroll(contactRef);

    const isMobile = dimensions.width < 768;

    return (
        <div className="app-container" style={{ minWidth: '100vw', backgroundColor: '#F0DFC3', overFlowX: 'hidden' }}>
            <NavigationMenu
                currentPage={currentPage}
                scrollToHome={scrollToHome}
                scrollToProjects={scrollToProjects}
                scrollToContact={scrollToContact}
                isMobile={isMobile}
            />

            <section
                ref={homeRef}
                className="page-section"
                style={{ height: `${dimensions.height}px`, width: '100%' }}
            >

                <ColorGrid />
                <Popup height={dimensions.height * 0.7} />
            </section>

            <section
                ref={projectsRef}
                className="page-section"
                style={{ minHeight: `${dimensions.height}px`, width: '100%' }}
            >
                <Projects />
            </section>
            <section
                ref={contactRef}
                className="page-section"
                style={{ minHeight: `${dimensions.height}px`, width: '100%' }}
            >
                <Contact />
            </section>
        </div>
    );
};

function Popup() {
    const [showPopup, setShowPopup] = useState(true);

    const closePopup = () => {
        setShowPopup(false);
    };

    return (
        <div>
            {showPopup && (
                <div style={{
                    position: 'absolute',
                    top: "60vh",
                    left: '50%',
                    transform: 'translateX(-50%)',
                    width: '320px', // Slightly wider for the extra text
                    padding: '35px 20px 20px 20px',
                    backgroundColor: '#F0DFC3',
                    boxShadow: '0 8px 16px rgba(0,0,0,0.25)',
                    borderRadius: '4px',
                    zIndex: 10,
                    border: '1px solid #dcc6a3'
                }}>
                    <button
                        onClick={closePopup}
                        style={{
                            position: 'absolute',
                            top: '10px',
                            right: '10px',
                            background: 'none',
                            border: 'none',
                            fontSize: '18px',
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            color: '#8b5e3c'
                        }}
                    >
                        ✕
                    </button>

                    <div style={{ marginBottom: '15px' }}>
                        <h3 style={{ margin: '0 0 5px 0', color: '#1a1a1a' }}>Gabriel House</h3>
                        <p style={{ margin: 0, fontSize: '0.85rem', color: '#007bff', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                            UWaterloo B. Computer Science
                        </p>
                    </div>

                    <p style={{ margin: '0 0 15px 0', fontSize: '0.95rem', color: '#444', lineHeight: '1.4' }}>
                        Cloud-native engineer focused on automated infrastructure and distributed systems.
                    </p>

                    <div style={{ borderTop: '1px solid #dcc6a3', paddingTop: '12px' }}>
                        <p style={{ margin: 0, fontSize: '0.9rem', fontWeight: '600', color: '#8b5e3c' }}>
                            Please click a blank space to leave a record of your visit.
                        </p>
                    </div>
                </div>
            )}
        </div>
    );
}

const NavigationMenu = ({ currentPage, scrollToHome, scrollToProjects, scrollToContact }) => {
    const getButtonStyle = (page) => ({
        border: 'none',
        background: 'none',
        fontSize: '16px',
        fontWeight: '600',
        // Dynamic color based on currentPage
        color: currentPage === page ? '#007bff' : '#333',
        cursor: 'pointer',
        margin: '0 15px',
        padding: '5px 0',
        outline: 'none',
        transition: 'color 0.2s ease',
        borderBottom: currentPage === page ? '2px solid #007bff' : '2px solid transparent'
    });

    return (
        <nav style={{
            position: 'fixed',
            top: '0',
            width: '100%',
            padding: '12px 0',
            backgroundColor: '#F0DFC3',
            zIndex: 1000,
            textAlign: 'center',
            boxShadow: '0 2px 10px rgba(0,0,0,0.05)'
        }}>
            <button onClick={scrollToHome} style={getButtonStyle("home")}>Home</button>
            <button onClick={scrollToProjects} style={getButtonStyle("projects")}>Projects</button>
            <button onClick={scrollToContact} style={getButtonStyle("contact")}>Contact</button>
        </nav>
    );
};

const ProfileHeader = () => {
    return (
        <div style={{
            padding: '20px',
            backgroundColor: '#fff',
            borderLeft: '4px solid #007bff',
            borderRadius: '4px',
            marginBottom: '40px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
        }}>
            <h3 style={{ margin: '0 0 10px 0', color: '#333' }}>🎓 System Origin: University of Waterloo</h3>
            <p style={{ margin: 0, color: '#555', lineHeight: '1.6' }}>
                <strong>Bachelor of Computer Science (BCS)</strong><br />
                Focused on distributed systems and software architecture. Graduated 2020.
            </p>
        </div>
    );
};

const ProjectCard = ({ project, index = 0 }) => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [zoomIndex, setZoomIndex] = useState(null);

    const navigateGallery = (direction, e) => {
        if (e) e.stopPropagation();
        const total = project.insights?.length || 0;
        if (total === 0) return;
        if (direction === 'next') {
            setZoomIndex((prev) => (prev + 1) % total);
        } else {
            setZoomIndex((prev) => (prev - 1 + total) % total);
        }
    };

    const renderMedia = (item, isLightbox = false, className = '') => {
        const src = item.video || item.image;
        if (!src) return null;

        const isVideo = src.endsWith('.webm') || src.endsWith('.mp4');
        const mediaStyle = className
            ? undefined
            : isLightbox
                ? { maxHeight: '70vh', borderRadius: '4px', cursor: 'default' }
                : {
                    width: '100%',
                    height: 'auto',
                    objectFit: 'contain',
                    backgroundColor: '#f8f4ec'
                };

        if (isVideo) {
            return (
                <video
                    src={src}
                    autoPlay
                    loop
                    playsInline
                    muted={!isLightbox}
                    controls={isLightbox}
                    className={className || undefined}
                    style={mediaStyle}
                    onClick={(e) => isLightbox && e.stopPropagation()}
                />
            );
        }

        return (
            <img
                src={src}
                alt={item.label}
                className={className || undefined}
                style={mediaStyle}
                loading="lazy"
                onClick={(e) => isLightbox && e.stopPropagation()}
            />
        );
    };

    useEffect(() => {
        const handleKeyDown = (e) => {
            if (e.key === 'Escape') {
                if (zoomIndex !== null) {
                    setZoomIndex(null);
                    return;
                }
                if (isModalOpen) setIsModalOpen(false);
                return;
            }
            if (zoomIndex === null) return;
            if (e.key === 'ArrowRight') navigateGallery('next');
            if (e.key === 'ArrowLeft') navigateGallery('prev');
        };
        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [zoomIndex, isModalOpen]);

    useEffect(() => {
        if (!isModalOpen) return;
        const previousOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.body.style.overflow = previousOverflow;
        };
    }, [isModalOpen]);

    const hasLiveEndpoint = project.link && !project.link.includes('github.com');
    const preview = project.insights?.[0];
    const cardNumber = String(index + 1).padStart(2, '0');
    const openModal = () => setIsModalOpen(true);
    const closeModal = () => {
        setZoomIndex(null);
        setIsModalOpen(false);
    };

    const details = (
        <div className="project-card__details">
            {project.featuredProduct && (
                <div className="project-card__featured">
                    <h4>Featured: {project.featuredProduct.title}</h4>
                    <p>{project.featuredProduct.description}</p>
                    <div className="project-card__featured-tech">
                        {project.featuredProduct.tech.map((t, i) => (
                            <span key={i}>{t}</span>
                        ))}
                    </div>
                </div>
            )}

            <ul className="project-card__highlights">
                {project.cloudHighlights.map((highlight, i) => (
                    <li key={i}>
                        <Markdown>{highlight}</Markdown>
                    </li>
                ))}
            </ul>

            {project.insights && (
                <div>
                    <h3 className="project-card__gallery-label">
                        System Architecture & Performance
                    </h3>
                    <div className="project-card__gallery">
                        {project.insights.map((item, i) => (
                            <div key={i} className="project-card__gallery-item">
                                <div
                                    className="project-card__gallery-thumb"
                                    onClick={() => setZoomIndex(i)}
                                >
                                    {renderMedia(item, false)}
                                </div>
                                <span>{item.label}</span>
                                <p>{item.description}</p>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            <div className="project-card__links">
                {project.infraRepo ? (
                    <>
                        <a
                            href={project.infraRepo}
                            target="_blank"
                            rel="noreferrer"
                            className="project-card__source is-infra"
                        >
                            <Gear size={16} weight="bold" />
                            Infra Source
                        </a>
                        <a
                            href={project.repo || project.serviceRepo}
                            target="_blank"
                            rel="noreferrer"
                            className="project-card__source"
                        >
                            <GithubLogo size={16} weight="bold" />
                            Service Source
                        </a>
                    </>
                ) : (
                    (project.repo || (project.link && project.link.includes('github.com'))) && (
                        <a
                            href={project.repo || project.link}
                            target="_blank"
                            rel="noreferrer"
                            className="project-card__source"
                        >
                            <GithubLogo size={16} weight="bold" />
                            View Source
                        </a>
                    )
                )}
            </div>
        </div>
    );

    return (
        <>
            <article className={`project-card${isModalOpen ? ' is-open' : ''}`}>
                {preview && (
                    <div className="project-card__media">
                        {renderMedia(preview, false, 'project-card__media-asset')}
                        <span className="project-card__index">{cardNumber}</span>
                    </div>
                )}

                <div className="project-card__body">
                    <div className="project-card__header">
                        <div className="project-card__heading">
                            <h2 className="project-card__title">
                                {!preview && <span className="project-card__index-inline">{cardNumber}</span>}
                                {project.title}
                            </h2>
                            <div className="project-card__actions">
                                {hasLiveEndpoint && (
                                    <a
                                        href={project.link}
                                        target="_blank"
                                        rel="noreferrer"
                                        className="project-card__chip project-card__chip--live"
                                    >
                                        Live Demo ↗
                                    </a>
                                )}
                                <button
                                    type="button"
                                    className="project-card__chip project-card__chip--details"
                                    onClick={openModal}
                                    aria-haspopup="dialog"
                                    aria-expanded={isModalOpen}
                                    aria-label="Details"
                                >
                                    ℹ️ Details
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="project-card__desc">
                        <Markdown>{project.description}</Markdown>
                    </div>

                    <div className="project-card__tags">
                        {project.technologies.map((tech, i) => (
                            <span key={i} className="project-card__tag">{tech}</span>
                        ))}
                    </div>
                </div>
            </article>

            {isModalOpen && (
                <div className="project-modal-overlay" onClick={closeModal}>
                    <div
                        className="project-modal"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby={`project-modal-title-${index}`}
                        onClick={(e) => e.stopPropagation()}
                    >
                        <button
                            type="button"
                            className="project-modal__close"
                            onClick={closeModal}
                            aria-label="Close project details"
                        >
                            <X size={18} weight="bold" />
                        </button>

                        <div className="project-modal__header">
                            <p className="project-modal__index">{cardNumber}</p>
                            <h2 id={`project-modal-title-${index}`} className="project-modal__title">
                                {project.title}
                            </h2>
                            {hasLiveEndpoint && (
                                <a
                                    href={project.link}
                                    target="_blank"
                                    rel="noreferrer"
                                    className="project-card__chip project-card__chip--live"
                                >
                                    Live Demo ↗
                                </a>
                            )}
                        </div>

                        <div className="project-modal__desc">
                            <Markdown>{project.description}</Markdown>
                        </div>

                        <div className="project-card__tags">
                            {project.technologies.map((tech, i) => (
                                <span key={i} className="project-card__tag">{tech}</span>
                            ))}
                        </div>

                        {details}
                    </div>
                </div>
            )}

            {zoomIndex !== null && project.insights && (
                <div
                    className="project-lightbox"
                    onClick={() => setZoomIndex(null)}
                >
                    <button
                        type="button"
                        className="project-lightbox__nav project-lightbox__nav--prev"
                        onClick={(e) => navigateGallery('prev', e)}
                    >‹</button>

                    <div className="project-lightbox__content">
                        {renderMedia(project.insights[zoomIndex], true)}
                        <div className="project-lightbox__caption">
                            <h3>{project.insights[zoomIndex].label}</h3>
                            <p>{project.insights[zoomIndex].description}</p>
                            <div className="project-lightbox__count">
                                {zoomIndex + 1} / {project.insights.length}
                            </div>
                        </div>
                    </div>

                    <button
                        type="button"
                        className="project-lightbox__nav project-lightbox__nav--next"
                        onClick={(e) => navigateGallery('next', e)}
                    >›</button>
                </div>
            )}
        </>
    );
};
// 2. The updated Projects component
const Projects = () => {
    const projects = [
        {
            title: "AWS Multi-Account Platform Template",
            repo: "https://github.com/gabehouse/aws-org-template",
            description: "A production-grade 'Infrastructure-as-Platform' template for bootstrapping a secure, multi-account AWS Organization with automated identity management.",
            technologies: ["Terraform", "AWS Organizations", "IAM Identity Center", "Docker", "VPC", "OIDC"],
            insights: [
                {
                    label: "Enterprise-Grade Landing Zone & Identity",
                    image: "assets/diagram-workspace-architecture.svg",
                    description: "A comprehensive multi-account strategy that isolates Management, Dev, and Prod workloads to minimize blast radius. By centralizing IAM Identity Center (SSO) and leveraging OIDC for keyless GitHub Actions deployments, the platform eliminates long-lived credentials while ensuring environment consistency across the organization."
                }
            ],
            cloudHighlights: [
                "Architected a **3-Account Strategy** (Management, Dev, Prod) using AWS Organizations to enforce strict administrative and billing boundaries.",
                "Implemented **IAM Identity Center (SSO)** with granular permission sets to eliminate the need for long-lived IAM user credentials and root-level access.",
                "Engineered a **Modular Networking Layer** featuring isolated VPCs and Security Group 'blueprints' to ensure consistent, secure connectivity across accounts.",
                "Integrated **GitHub Actions via OIDC** for keyless, short-lived credential exchange, enabling secure CI/CD pipelines across the entire organization.",
                "Provisioned a **Dockerized Devcontainer** to provide a consistent, pre-configured development environment for local Terraform and AWS CLI operations."
            ]
        },
        {
            title: "Wilderchess: ML-Driven Strategy Engine",
            infraRepo: "https://github.com/gabehouse/aws-org-workspace/tree/master/infra/workloads/dev/wilderchess",
            serviceRepo: "https://github.com/gabehouse/aws-org-workspace/tree/master/services/wilderchess",
            link: "http://wilderchess.eba-swezjps7.us-east-2.elasticbeanstalk.com/",
            description: "A real-time multiplayer PvP game powered by a custom-trained Reinforcement Learning agent and a high-concurrency Java backend.",
            technologies: ["Java 21", "Terraform", "AWS Spot Fleet", "S3", "ECR", "ONNX", "WebSockets", "OIDC"],
            insights: [
                {
                    label: "Hybrid ML & Infrastructure Lifecycle",
                    image: "/assets/diagram-wilderchess-architecture.svg",
                    description: "A dual-tier architecture utilizing a cost-optimized EC2 Spot Fleet for high-volume data generation and an Elastic Beanstalk production environment. The workflow features a 'Hybrid' training loop: syncing raw data from S3 to a local GPU workstation for training, and deploying optimized ONNX artifacts back to the cloud for real-time inference."
                },
                {
                    label: "Dynamic Recurrence Visualization",
                    video: "assets/wilderchess-demo.webm", // Point to the WebM file
                    description: "Live gameplay showcasing the Reinforcement Learning agent’s preference for positional control and piece activity. Unlike heuristic-based bots, the model identifies non-obvious tactical sacrifices to maximize board influence."
                },

                {
                    label: "Game Balance Convergence",
                    image: "assets/portfolio_convergence_chart.png",
                    description: "Empirical validation of agent performance across 2,300 simulated games. The Heuristic Baseline confirmed a stable 49.5% win-rate, while the Reinforcement Learning agent achieved a statistically significant 82.9% win-rate (95% CI via Wilson Score Interval), proving a tactical advantage over rule-based logic."
                },
                {
                    label: "Strategy Heatmap",
                    image: "assets/strategy_heatmap.png", // Your action selection analysis
                    description: "Visualizing the Neural Network's preference for tactical positioning over the 'Medium' bot's heuristic-based movement."
                },
                {
                    label: "Inference Latency",
                    image: "assets/InferenceLatency.png", // CloudWatch metrics
                    description: "Sub-millisecond inference performance achieved via ONNX Runtime integration on AWS Elastic Beanstalk."
                }
            ],
            cloudHighlights: [
                "Architected a **Spot Instance Fleet** via ASG Mixed Instances Policy, orchestrating a high-node cluster to achieve ~80% cost reduction for ML data generation.",
                "Engineered an **Automated Data Pipeline**: Distributed Docker runners generate game-state datasets, synced via Cron to S3 for centralized model training and evaluation.",
                "Deployed a high-availability **Java Corretto 21** stack on Elastic Beanstalk, utilizing **ALB Sticky Sessions** to maintain persistent WebSocket state for active games.",
                "Implemented **Infrastructure-as-Code** via modular Terraform, managing environment state with S3 backends and enforcing granular IAM security boundaries.",
                "Optimized **Real-Time Inference** by integrating the ONNX Runtime directly into the Java server, enabling the RL model to execute moves in under 1ms.",
                "Developed a **secure CI/CD Pipeline** using **GitHub Actions** and **OpenID Connect (OIDC)**, automating **Maven builds** and **Terraform deployments** to AWS without persistent credentials, ensuring **100% reproducible environments**.",
                "Architected a local **Jenkins CI pipeline using Docker-out-of-Docker (DooD)**, automating containerized builds on every `dev` branch push to ensure local environment health before cloud staging."
            ]
        },
        {
            title: "House Audio (Full-Stack Engine) ",
            link: "https://houseaudio.net",
            infraRepo: "https://github.com/gabehouse/aws-org-workspace/tree/master/infra/workloads/prod/vstshop",
            serviceRepo: "https://github.com/gabehouse/aws-org-workspace/tree/master/services/vstshop-frontend",
            description: "A production-grade storefront and distribution platform for high-performance audio software, featuring automated Stripe fulfillment and secure asset delivery.",
            technologies: ["React", "Terraform", "AWS Lambda", "DynamoDB", "Cognito", "Stripe API", "OIDC", "Route 53"],
            featuredProduct: {
                title: "Acid Saturator VST",
                tech: ["C++", "JUCE", "DSP"],
                description: "Professional-grade audio plugin featuring custom non-linear distortion algorithms, serving as the flagship product for the platform."
            },
            insights: [
                {
                    label: "Serverless Architecture Overview",
                    image: "/assets/diagram-vstshop-architecture.svg",
                    description: "A robust serverless architecture featuring OIDC-based authentication, event-driven Stripe fulfillment via Webhooks, and secure asset distribution."
                },
                {
                    label: "Secure Digital Distribution",
                    image: "/assets/acid-saturator-demo.webm",
                    description: "End-to-end purchase flow: Stripe events trigger a Lambda-based validation service that generates short-lived S3 Presigned URLs, ensuring content is only accessible to authorized customers."
                }
            ],
            cloudHighlights: [
                "Engineered an **Event-Driven Fulfillment Pipeline**: Stripe Webhooks trigger asynchronous DynamoDB state updates and Lambda-generated **S3 Presigned URLs** for secure digital asset delivery.",
                "Implemented **Infrastructure-as-Code (IaC)** via Terraform to orchestrate a modular serverless stack, including CloudFront distributions and automated **ACM Certificate validation via Route 53**.",
                "Orchestrated **Identity Federation via Amazon Cognito**, utilizing **OIDC (Google Social Login)** to secure downstream API access while eliminating the need for managed credential storage.",
                "Integrated **GitHub Actions with OIDC** for 'keyless' CI/CD, automating high-availability deployments for React and Python-based microservices without persistent IAM secrets.",
                "Architected a **Containerized Development Environment** using Docker Devcontainers to ensure 1:1 environment parity between local development and AWS Lambda production runtimes."
            ]
        },
        {
            title: "Courts: Matchmaking Platform",
            link: "https://master.dt5mmfwcef1et.amplifyapp.com//",
            repo: "https://github.com/gabehouse/aws-org-workspace/tree/master/services/court-app",
            // Focus on "Event-Driven" and "Secure" in the summary
            description: "A real-time geospatial matchmaking platform enabling players to discover local courts, broadcast availability, and challenge opponents.",
            technologies: ["React", "Amplify Gen 2", "TypeScript", "AppSync", "DynamoDB", "Cognito", "Leaflet/Maps"],
            insights: [
                {
                    label: "Event-Driven Booking Pipeline",
                    image: "/assets/diagram-tennis-booking-architecture.svg",
                    description: "An event-driven serverless architecture using Amplify Gen 2 and AppSync WebSockets to sync live challenge requests and player coordinates across active map sessions."
                }
            ],
            cloudHighlights: [
                "Architected using **Amplify Gen 2** with TypeScript-defined backend infrastructure (AppSync GraphQL, DynamoDB, and Cognito) for type-safe cloud development.",
                "Implemented real-time **Challenge and Messaging Workflows** using WebSockets to ensure instant updates when players accept or request matches.",
                "Integrated **Geospatial Query Patterns** in DynamoDB to efficiently filter and render nearby players and courts dynamically on the map UI.",
                "Configured secure **Social Identity Federation** via AWS Cognito and OIDC, streamlining user onboarding while preserving strict data isolation.",
                "Automated continuous deployment via Git-based CI/CD pipelines, spinning up isolated full-stack preview environments on every branch push."
            ]
        },
        {
            title: "Cloud-Native Engineering Portfolio",
            link: "https://master.d1gyqq9jpvj1mt.amplifyapp.com/",
            repo: "https://github.com/gabehouse/aws-org-workspace/tree/master/services/cloud-portfolio",
            description: "A self-deploying, high-availability professional platform engineered with serverless primitives and automated certificate lifecycle management.",
            technologies: ["React", "AWS Amplify", "DynamoDB", "ACM", "GitHub Actions", "Cloudflare"],
            insights: [
                {
                    label: "Cloud-Native Architecture & Deployment Pipeline",
                    image: "/assets/diagram-cloud-portfolio-architecture.svg",
                    description: "A comprehensive visualization of the hybrid-cloud lifecycle. The architecture showcases the integration between Cloudflare’s global DNS and AWS’s serverless edge, utilizing Amplify Gen 2 for Infrastructure-from-Code (IfC). The diagram illustrates the automated CI/CD loop—where GitHub webhooks trigger isolated environment branching—and the serverless analytics flow from Lambda to DynamoDB."
                }
            ],
            cloudHighlights: [
                "Architected an **Automated CI/CD Pipeline** via AWS Amplify, orchestrating a full build-test-deploy lifecycle with isolated environment branching for feature previews.",
                "Provisioned a **Serverless Analytics Engine** using **DynamoDB** and **Lambda**, leveraging **Infrastructure-from-Code (IfC)** to define type-safe data schemas and background compute.",
                "Engineered a **Hybrid-Cloud Networking Strategy** using **Cloudflare** and **CloudFront**, optimizing global edge caching and SSL/TLS termination to minimize Time to First Byte (TTFB).",
                "Implemented **Scalable Asset Management** via **Amazon S3**, utilizing automated lifecycle policies and cloud-native distribution for high-performance frontend delivery.",
                "Leveraged **Amplify Gen 2** for seamless **Environment Parity**, ensuring consistent configuration of backend cloud resources between development and production branches."
            ]
        },
        {
            title: "Needleman-Wunsch: Algorithmic Lab",
            repo: "https://github.com/gabehouse/Needleman-Wunsch-Demo",

            link: "https://gabehouse.github.io/Needleman-Wunsch-Demo/",
            description: "An interactive bioinformatics engine for global sequence alignment, optimized for O(n × m) computational complexity.",
            technologies: ["React", "JavaScript", "Jest", "GitHub Pages"],
            insights: [
                {
                    label: "Dynamic Recurrence Visualization",
                    video: "../assets/nw-algo-demo.webm",
                    description: "High-fidelity visualization of the O(n × m) matrix filling and optimal path backtracking, utilizing optimized DOM rendering to prevent layout thrashing during large-scale calculations."
                }
            ],
            cloudHighlights: [
                "Implemented rigorous **Unit Testing via Jest** for the core Dynamic Programming recurrence, ensuring 100% accuracy for edge-case biological sequence comparisons.",
                "Optimized client-side compute to handle large-scale matrices without blocking the **Main UI Thread**, maintaining a fluid 60 FPS experience during heavy algorithmic processing.",
                "Engineered a **Static Site Delivery** strategy leveraging GitHub's global CDN, providing a zero-cost, high-availability hosting model with minimal latency.",
                "Optimized the **Frontend Rendering Engine** to manage large grid states efficiently, preventing memory leaks and UI stutter during real-time matrix generation.",
                "Designed the algorithmic core with **Functional Programming principles**, allowing for isolated testing and modular extension of different scoring matrices (e.g., BLOSUM62).",
                "Leveraged **GitHub Pages** for production hosting, ensuring a reliable, SSL-encrypted entry point for the professional portfolio."
            ]
        }
    ];

    return (
        <div className="projects-section">
            <p className="projects-section__eyebrow">Selected Work</p>
            <h1 className="projects-section__title">Technical Projects</h1>
            <p className="projects-section__lede">
                Cloud-native systems spanning multi-account AWS platforms, real-time inference, and serverless product delivery.
            </p>
            <div className="projects-section__grid">
                {projects.map((project, index) => (
                    <ProjectCard
                        key={index}
                        project={project}
                        index={index}
                    />
                ))}
            </div>
        </div>
    );
};

const Contact = ({ isMobile }) => {
    const contacts = [
        {
            service: "IAM / Identity",
            title: "LinkedIn",
            icon: <LinkedinLogo size={24} weight="light" />,
            value: "linkedin.com/in/gabriel-house",
            action: "https://linkedin.com/in/gabriel-house",
            description: "Professional background and technical endorsements."
        },
        {
            service: "Source Control",
            title: "GitHub",
            icon: <GithubLogo size={24} weight="light" />,
            value: "github.com/gabehouse",
            action: "https://github.com/gabehouse",
            description: "IaC repositories, Terraform modules, and project source."
        },
        {
            service: "SMTP / Messaging",
            title: "Email",
            icon: <Envelope size={24} weight="light" />,
            value: "gabriel.jsh@gmail.com",
            action: "mailto:gabriel.jsh@gmail.com",
            description: "Direct line for inquiries and technical collaboration."
        }
    ];

    return (
        <div style={{
            padding: '60px 5%',
            width: '100%',
            maxWidth: '900px',
            margin: '0 auto', // CRITICAL: Centers the block in the viewport
            boxSizing: 'border-box',
        }}>
            <h1 style={{
                marginBottom: '10px',
                color: 'inherit', // Uses color from your CSS :root
                textAlign: isMobile ? 'left' : 'center' // Optional: Centers header on desktop
            }}>
                Connection Endpoints
            </h1>
            <p style={{
                color: 'var(--text-secondary, #666)',
                marginBottom: '40px',
                fontSize: '1.1rem',
                textAlign: isMobile ? 'left' : 'center'
            }}>
                Reach out via the verified service endpoints below.
            </p>

            <div style={{
                display: 'grid',
                gridTemplateColumns: isMobile ? '100%' : 'repeat(auto-fit, minmax(280px, 1fr))',
                gap: '20px',
            }}>
                {contacts.map((contact, index) => (
                    <div key={index} style={{
                        padding: '24px',
                        backgroundColor: 'var(--card-bg, #fcfaf2)', // Dynamic bg
                        borderRadius: '8px',
                        border: '1px solid var(--border-color, #fcfaf2)',
                        transition: 'transform 0.2s ease-in-out',
                        display: 'flex',
                        flexDirection: 'column',
                        justifyContent: 'space-between'
                    }}>
                        <div>
                            <span style={{
                                fontSize: '0.75rem',
                                fontWeight: '700',
                                color: '#646cff', // Matches your global link color
                                textTransform: 'uppercase',
                                letterSpacing: '1px'
                            }}>
                                {contact.service}
                            </span>

                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', margin: '12px 0 8px 0' }}>
                                <span style={{ color: '#646cff', display: 'flex' }}>
                                    {contact.icon}
                                </span>
                                <h2 style={{ margin: 0, fontSize: '1.5rem', color: 'inherit' }}>
                                    {contact.title}
                                </h2>
                            </div>

                            <p style={{ color: 'inherit', opacity: 0.8, fontSize: '0.9rem', marginBottom: '15px', lineHeight: '1.6' }}>
                                {contact.description}
                            </p>
                        </div>

                        <a href={contact.action} target="_blank" rel="noopener noreferrer"
                            style={{
                                color: '#646cff',
                                textDecoration: 'none',
                                fontWeight: '600',
                                fontSize: '1rem',
                                wordBreak: 'break-all',
                                marginTop: 'auto'
                            }}>
                            {contact.value} →
                        </a>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default App;
