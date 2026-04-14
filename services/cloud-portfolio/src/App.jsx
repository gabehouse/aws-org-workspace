import React, { useRef, useState, useEffect } from 'react';
import ColorGrid from './ColorGrid';
import Markdown from 'react-markdown';
import { GithubLogo, LinkedinLogo, Envelope } from "@phosphor-icons/react";

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

function Popup({ height }) {
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

const NavigationMenu = ({ currentPage, scrollToHome, scrollToProjects, scrollToContact, isMobile }) => {
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

const ProjectCard = ({ project }) => {
    const [isExpanded, setIsExpanded] = useState(false);
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

    // Updated Helper: Fixes thumbnail readability
    const renderMedia = (item, isLightbox = false) => {
        const src = item.video || item.image;
        if (!src) return null;

        const isVideo = src.endsWith('.webm') || src.endsWith('.mp4');

        // CHANGE: Use 'contain' for thumbnails to show the full diagram/text
        const mediaStyle = isLightbox
            ? { maxHeight: '70vh', borderRadius: '4px', cursor: 'default' }
            : {
                width: '100%',
                height: 'auto',
                objectFit: 'contain', // Changed from 'cover'
                backgroundColor: '#f8f9fa' // Matches the container to hide "bars"
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
                    style={mediaStyle}
                    onClick={(e) => isLightbox && e.stopPropagation()}
                />
            );
        }

        return (
            <img
                src={src}
                alt={item.label}
                style={mediaStyle}
                loading="lazy" // Performance optimization for gallery
                onClick={(e) => isLightbox && e.stopPropagation()}
            />
        );
    };

    useEffect(() => {
        const handleKeyDown = (e) => {
            if (zoomIndex === null) return;
            if (e.key === 'ArrowRight') navigateGallery('next');
            if (e.key === 'ArrowLeft') navigateGallery('prev');
            if (e.key === 'Escape') setZoomIndex(null);
        };
        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [zoomIndex]);

    return (
        <div
            style={{
                marginBottom: '25px',
                padding: '24px',
                backgroundColor: '#fcfaf2',
                borderRadius: '8px',
                boxShadow: isExpanded ? '0 10px 30px rgba(0,0,0,0.1)' : '0 2px 8px rgba(0,0,0,0.05)',
                transition: 'all 0.3s ease',
                border: '1px solid #e0d0b0', // Restored beige border
                width: '100%',
                boxSizing: 'border-box'
            }}
        >
            {/* LIGHTBOX */}
            {zoomIndex !== null && project.insights && (
                <div
                    onClick={() => setZoomIndex(null)}
                    style={{
                        position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh',
                        backgroundColor: 'rgba(0,0,0,0.9)', zIndex: 1000,
                        display: 'flex', justifyContent: 'center', alignItems: 'center',
                        cursor: 'zoom-out'
                    }}
                >
                    <button
                        onClick={(e) => navigateGallery('prev', e)}
                        style={{ position: 'absolute', left: '30px', background: 'none', border: 'none', color: '#fff', fontSize: '3rem', cursor: 'pointer' }}
                    >‹</button>

                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', maxWidth: '85%' }}>
                        {renderMedia(project.insights[zoomIndex], true)}
                        <div style={{ color: '#fff', marginTop: '20px', textAlign: 'center' }}>
                            <h3 style={{ margin: '0' }}>{project.insights[zoomIndex].label}</h3>
                            <p style={{ opacity: 0.8 }}>{project.insights[zoomIndex].description}</p>
                            <div style={{ fontSize: '0.8rem', opacity: 0.5, marginTop: '10px' }}>
                                {zoomIndex + 1} / {project.insights.length}
                            </div>
                        </div>
                    </div>

                    <button
                        onClick={(e) => navigateGallery('next', e)}
                        style={{ position: 'absolute', right: '30px', background: 'none', border: 'none', color: '#fff', fontSize: '3rem', cursor: 'pointer' }}
                    >›</button>
                </div>
            )}

            {/* HEADER */}
            <div onClick={() => setIsExpanded(!isExpanded)} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', userSelect: 'none' }}>
                <h2 style={{ margin: 0, fontSize: '1.5rem', color: '#333' }}>{project.title}</h2>
                <span style={{ fontSize: '1rem', color: '#007bff', fontWeight: 'bold' }}>
                    {isExpanded ? 'VIEW LESS −' : 'VIEW CLOUD INFRA +'}
                </span>
            </div>

            <div style={{ color: '#555', margin: '15px 0', lineHeight: '1.5' }}>
                <Markdown>{project.description}</Markdown>
            </div>

            {/* TECH STACK */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginBottom: isExpanded ? '20px' : '0' }}>
                {project.technologies.map((tech, i) => (
                    <span key={i} style={{ fontSize: '0.8rem', padding: '4px 10px', backgroundColor: '#f0f0f0', borderRadius: '12px', color: '#666' }}>
                        {tech}
                    </span>
                ))}
            </div>

            {isExpanded && (
                <div style={{ marginTop: '20px', paddingTop: '20px', borderTop: '1px solid #eee' }}>

                    {/* RESTORED BEIGE FEATURED PRODUCT SECTION */}
                    {project.featuredProduct && (
                        <div style={{
                            backgroundColor: '#faf7f2', // Classic beige background
                            border: '1px solid #e0d0b0',
                            borderRadius: '6px',
                            padding: '20px',
                            marginBottom: '25px'
                        }}>
                            <h4 style={{ margin: '0 0 10px 0', color: '#8b5e3c', fontSize: '1.1rem' }}>
                                🎹 Featured: {project.featuredProduct.title}
                            </h4>
                            <p style={{ margin: '0 0 12px 0', fontSize: '0.9rem', color: '#555', lineHeight: '1.5' }}>
                                {project.featuredProduct.description}
                            </p>
                            <div style={{ display: 'flex', gap: '10px' }}>
                                {project.featuredProduct.tech.map((t, i) => (
                                    <span key={i} style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#8b5e3c' }}>{t}</span>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* CLOUD HIGHLIGHTS */}
                    <div style={{ marginBottom: '25px' }}>
                        <ul style={{ paddingLeft: '1.2rem', margin: 0 }}>
                            {project.cloudHighlights.map((highlight, i) => (
                                <li key={i} style={{ color: '#444', fontSize: '0.95rem', marginBottom: '8px' }}>
                                    <Markdown>{highlight}</Markdown>
                                </li>
                            ))}
                        </ul>
                    </div>

                    {/* GALLERY */}
                    {project.insights && (
                        <div style={{ marginBottom: '25px' }}>
                            <h3 style={{
                                fontSize: '0.85rem',
                                color: '#8b5e3c',
                                textTransform: 'uppercase',
                                letterSpacing: '1px',
                                marginBottom: '12px',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px'
                            }}>
                                <span style={{ fontSize: '1.1rem' }}>🏗️</span>
                                System Architecture & Performance Validation
                            </h3>
                            <div style={{ display: 'flex', gap: '15px', overflowX: 'auto', paddingBottom: '10px' }}>
                                {project.insights.map((item, i) => (
                                    <div key={i} style={{ flex: '0 0 280px' }}>
                                        <div
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                setZoomIndex(i);
                                            }}
                                            style={{
                                                width: '100%',
                                                maxHeight: '260px',
                                                // height: '160px', // REMOVE THIS
                                                backgroundColor: '#f8f9fa',
                                                borderRadius: '6px',
                                                overflow: 'hidden',
                                                marginBottom: '8px',
                                                cursor: 'zoom-in',
                                                border: '1px solid #eee',
                                                display: 'flex', // Ensures content aligns correctly
                                                alignItems: 'center'
                                            }}
                                        >
                                            {renderMedia(item, false)}
                                        </div>
                                        <span style={{ fontSize: '0.85rem', fontWeight: '600' }}>{item.label}</span>
                                        <p style={{ fontSize: '0.75rem', color: '#666', marginTop: '4px' }}>{item.description}</p>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* LINKS SECTION */}
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '20px', marginTop: '10px' }}>

                        {/* 1. Live Site (Standard) */}
                        {project.link && !project.link.includes('github.com') && (
                            <a href={project.link} target="_blank" rel="noreferrer"
                                style={{ color: '#007bff', textDecoration: 'none', fontWeight: '600' }}>
                                Live Site ↗
                            </a>
                        )}

                        {/* 2. Split Source (Monorepo Logic) */}
                        {project.infraRepo ? (
                            <>
                                <a
                                    href={project.infraRepo}
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{ color: '#8b5e3c', textDecoration: 'none', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '4px' }}
                                >
                                    <span style={{ fontSize: '1rem' }}>☁️</span> Infra Source ↗
                                </a>
                                <a
                                    href={project.repo || project.serviceRepo}
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{ color: '#333', textDecoration: 'none', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '4px' }}
                                >
                                    <span style={{ fontSize: '1rem' }}>📦</span> Service Source ↗
                                </a>
                            </>
                        ) : (
                            /* 3. Single Source (Standalone Repo Logic) */
                            (project.repo || (project.link && project.link.includes('github.com'))) && (
                                <a
                                    href={project.repo || project.link}
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{ color: '#333', textDecoration: 'none', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '4px' }}
                                >
                                    View Source ↗
                                </a>
                            )
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};
// 2. The updated Projects component
const Projects = ({ isMobile }) => {
    const projects = [
        {
            title: "AWS Multi-Account Platform Template",
            repo: "https://github.com/gabehouse/aws-org-template",
            description: "A production-grade 'Infrastructure-as-Platform' template for bootstrapping a secure, multi-account AWS Organization with automated identity management.",
            technologies: ["Terraform", "AWS Organizations", "IAM Identity Center", "Docker", "VPC", "OIDC"],
            insights: [
                {
                    label: "Enterprise-Grade Landing Zone & Identity",
                    image: "assets/diagram-workspace.svg",
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
            technologies: ["Java 21", "Terraform", "AWS Spot Fleet", "S3", "ECR", "ONNX", "WebSockets"],
            insights: [
                {
                    label: "Hybrid ML & Infrastructure Lifecycle",
                    image: "/assets/diagram-wilderchess.svg",
                    description: "A dual-tier architecture utilizing a cost-optimized EC2 Spot Fleet for high-volume data generation and an Elastic Beanstalk production environment. The workflow features a 'Hybrid' training loop: syncing raw data from S3 to a local GPU workstation for training, and deploying optimized ONNX artifacts back to the cloud for real-time inference."
                },
                {
                    label: "Dynamic Recurrence Visualization",
                    video: "assets/wilderchess-demo.webm", // Point to the WebM file
                    description: "High-fidelity visualization of the O(n x m) matrix filling and optimal path backtracking, rendered without color-banding."
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
                "Optimized **Real-Time Inference** by integrating the ONNX Runtime directly into the Java server, enabling the RL model to execute moves in under 1ms."
            ]
        },
        {
            title: "House Audio (Full-Stack Engine)",
            link: "https://houseaudio.net",
            infraRepo: "https://github.com/gabehouse/aws-org-workspace/tree/master/infra/workloads/prod/vstshop",
            serviceRepo: "https://github.com/gabehouse/aws-org-workspace/tree/master/services/vstshop-frontend",
            description: "A production-grade storefront and distribution platform for high-performance audio software, featuring automated Stripe fulfillment and secure asset delivery.",
            technologies: ["React", "Terraform", "AWS Lambda", "DynamoDB", "Cognito", "Stripe API", "OIDC"],
            featuredProduct: {
                title: "Acid Saturator VST",
                tech: ["C++", "JUCE", "DSP"],
                description: "Professional-grade audio plugin featuring custom non-linear distortion algorithms, serving as the flagship product for the platform."
            },
            insights: [
                {
                    label: "Serverless Architecture Overview",
                    image: "/assets/diagram-vstshop.svg",
                    description: "A robust serverless architecture featuring OIDC-based authentication, event-driven Stripe fulfillment via Webhooks, and secure asset distribution."
                },
                {
                    label: "Secure Digital Distribution",
                    image: "/assets/acid-saturator-demo.webm",
                    description: "End-to-end purchase flow: Stripe events trigger a Lambda-based validation service that generates short-lived S3 Presigned URLs, ensuring content is only accessible to authorized customers."
                }
            ],
            cloudHighlights: [
                "Orchestrated **Identity Federation via Amazon Cognito**, utilizing **OIDC (Google Social Login)** to secure downstream API access while providing a passwordless user experience.",
                "Engineered a serverless **Fulfillment Pipeline**: Stripe Webhooks trigger asynchronous DynamoDB state updates and Lambda-generated **S3 Presigned URLs** for secure, temporary download access.",
                "Implemented **Infrastructure-as-Code (IaC)** via Terraform to manage a scalable serverless stack, including CloudFront distribution and granular IAM execution roles.",
                "Integrated **GitHub Actions with OIDC** for 'keyless' CI/CD, automating high-availability deployments for the React frontend and Python-based Lambda microservices.",
                "Architected a **Containerized Development Environment** using Docker Devcontainers to ensure 1:1 environment parity between local development and AWS Lambda production runtimes."
            ]
        },
        {
            title: "Grand River Tennis Lessons",
            link: "https://master.dkskd07qtjixa.amplifyapp.com/",
            repo: "https://github.com/gabehouse/aws-org-workspace/tree/master/services/tennis-site",
            // Focus on "Event-Driven" and "Secure" in the summary
            description: "A full-stack booking platform featuring an event-driven serverless backend and secure OIDC-based identity federation.",
            technologies: ["React", "Amplify Gen 2", "TypeScript", "Lambda", "DynamoDB", "Cognito", "OIDC"],
            insights: [
                {
                    label: "Event-Driven Booking Pipeline",
                    image: "/assets/diagram-tennis-booking.svg",
                    description: "Amplify Gen 2 backend where DynamoDB Streams trigger Lambda functions for automated SES alerts. This decouples the booking logic from the notification system, ensuring high availability during peak registration windows."
                }
            ],
            cloudHighlights: [
                "Architected using **Amplify Gen 2**, utilizing a Git-based **CI/CD Pipeline** that automates full-stack deployments on every branch push.",
                "Engineered **Social Identity Federation** via AWS Cognito and OIDC, managing secure user sessions without managing sensitive credential data.",
                "Implemented **Infrastructure-from-Code (IfC)** using TypeScript to define scalable backend resources including AppSync (GraphQL) and DynamoDB.",
                "Built a real-time notification engine using **DynamoDB Streams**, decoupling high-latency email/SMS tasks from the core booking transaction.",
                "Configured **RBAC (Role-Based Access Control)** to strictly isolate administrative dashboard access from student booking views."
            ]
        },
        {
            title: "Cloud-Native Engineering Portfolio",
            link: "https://master.d1gyqq9jpvj1mt.amplifyapp.com/",
            repo: "https://github.com/gabehouse/aws-org-workspace/tree/master/services/cloud-portfolio",
            description: "A self-deploying, high-availability professional platform engineered with serverless primitives and automated certificate lifecycle management.",
            technologies: ["React", "AWS Amplify", "DynamoDB", "Route 53", "ACM", "GitHub Actions"],
            cloudHighlights: [
                "Architected an **Automated CI/CD Pipeline** via AWS Amplify, orchestrating the full build-test-deploy lifecycle triggered by GitHub Webhooks.",
                "Implemented **Automated Certificate Provisioning** and renewal using AWS Certificate Manager (ACM), ensuring consistent SSL/TLS encryption across all environments.",
                "Configured **Apex Domain Mapping** and global DNS routing via Route 53, leveraging AWS’s global edge network to minimize request latency.",
                "Provisioned a **Serverless Analytics Engine** using DynamoDB and Lambda to track visitor engagement metrics with zero-management overhead.",
                "Leveraged **Amplify Environment Branching** to maintain strict isolation between 'Preview' feature branches and the stable production environment.",
                "Optimized global content delivery by utilizing **CloudFront's edge locations** (via Amplify) to reduce Time to First Byte (TTFB) for international visitors."
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
        },
        {
            title: "JS Physics Lab: Kinetic Engine",
            link: "https://gabehouse.github.io/js-physics-demo/",
            repo: "https://github.com/gabehouse/js-physics-demo",
            description: "A high-performance 2D physics simulation engineered in vanilla JavaScript, featuring real-time collision detection and momentum conservation logic.",
            technologies: ["JavaScript", "HTML5 Canvas", "CSS3", "GitHub Pages"],
            insights: [
                {
                    label: "Collision & Momentum Transfer",
                    image: "../assets/ball-physics-demo.webm",
                    description: "Visualizing impulse-based collision resolution and energy conservation. The engine utilizes a custom vector math library to calculate reflection vectors and resolve kinetic energy transfer across high-velocity entities."
                }
            ],
            cloudHighlights: [
                "Engineered a high-performance **2D Rendering Engine** using HTML5 Canvas, decoupling physics state updates from browser draw calls to maintain a smooth user experience.",
                "Optimized the **Animation Render Loop** using `requestAnimationFrame`, achieving a consistent 60 FPS under varying computational loads.",
                "Developed a custom **Vector Mathematics Library** from scratch to handle Euclidean distance calculations and reflection vectors for realistic kinetic interactions.",
                "Implemented an efficient **Collision Detection Algorithm** designed to minimize O(n²) computational overhead, allowing for high-density particle simulations.",
                "Leveraged **GitHub Pages** for static site delivery, utilizing a global CDN to ensure high availability and low-latency access for international users.",
                "Designed the engine with a **Modular Architecture**, enabling the easy addition of physical properties like gravity, friction, and elasticity without refactoring core logic."
            ]
        }
    ];

    return (
        <div style={{ padding: '80px 5%', maxWidth: '900px', margin: '0 auto' }}>
            <h1 style={{ marginBottom: '40px', color: '#333', fontSize: '2.5rem' }}>Technical Projects</h1>
            {projects.map((project, index) => (
                <ProjectCard key={index} project={project} />
            ))}
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