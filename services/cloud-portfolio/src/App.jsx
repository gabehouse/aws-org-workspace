import { useRef, useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import Markdown from 'react-markdown';
import './ProjectCards.css';
import { GithubLogo } from "@phosphor-icons/react/dist/icons/GithubLogo";
import { LinkedinLogo } from "@phosphor-icons/react/dist/icons/LinkedinLogo";
import { Envelope } from "@phosphor-icons/react/dist/icons/Envelope";
import { X } from "@phosphor-icons/react/dist/icons/X";
import { Gear } from "@phosphor-icons/react/dist/icons/Gear";

const EMAIL = "gabriel.jsh@gmail.com";
const GITHUB = "https://github.com/gabehouse";
const LINKEDIN = "https://linkedin.com/in/gabriel-house";

const systems = [
    {
        title: "AWS Multi-Account Setup",
        repo: "https://github.com/gabehouse/aws-org-template",
        summary: "The AWS foundation everything else here runs on. Separate accounts for management, dev, and prod, all written in Terraform.",
        technologies: ["Terraform", "AWS Organizations", "IAM Identity Center", "VPC", "GitHub OIDC"],
        proof: [
            "Dev and prod are different accounts, so a mistake in dev can't reach prod.",
            "I sign in through IAM Identity Center instead of IAM users. Prod sessions are shorter, and they can't delete the Terraform state bucket.",
            "GitHub Actions deploys with OIDC, so there are no AWS keys sitting in GitHub."
        ],
        figures: [
            {
                label: "How the accounts fit together",
                image: "/assets/diagram-workspace-architecture.svg",
                description: "Management at the top, dev and prod underneath, each with its own network."
            }
        ]
    },
    {
        title: "House Audio",
        link: "https://houseaudio.net",
        infraRepo: "https://github.com/gabehouse/aws-org-workspace/tree/master/infra/workloads/prod/vstshop",
        serviceRepo: "https://github.com/gabehouse/aws-org-workspace/tree/master/services/vstshop-frontend",
        summary: "A small store for an audio plugin I made. You sign in with Google, pay with Stripe, and download the file.",
        technologies: ["Lambda", "API Gateway", "DynamoDB", "Cognito", "S3", "CloudFront", "Stripe", "Terraform"],
        proof: [
            "Stripe webhooks are signature-checked before anything gets written.",
            "Before handing out a download, a Lambda checks that you actually bought the plugin. The link it gives you expires in 15 minutes.",
            "The whole stack is in Terraform, from CloudFront down to the SSL certificate."
        ],
        figures: [
            {
                label: "From checkout to download",
                image: "/assets/diagram-vstshop-architecture.svg",
                description: "Stripe tells one Lambda about the payment. A second Lambda checks the purchase and hands back a short-lived link."
            },
            {
                label: "The plugin",
                video: "/assets/acid-saturator-demo.webm",
                description: "Acid Saturator, written in C++ with JUCE."
            }
        ]
    },
    {
        title: "Wilderchess",
        link: "http://wilderchess.eba-swezjps7.us-east-2.elasticbeanstalk.com/",
        infraRepo: "https://github.com/gabehouse/aws-org-workspace/tree/master/infra/workloads/dev/wilderchess",
        serviceRepo: "https://github.com/gabehouse/aws-org-workspace/tree/master/services/wilderchess",
        summary: "A multiplayer game with a bot I trained. The bot learned from games played on cheap Spot instances.",
        technologies: ["EC2 Spot", "Elastic Beanstalk", "S3", "ECR", "Java", "ONNX", "Terraform"],
        proof: [
            "Training games run on EC2 Spot for about 80% less than regular instances. If one gets shut down, I lose a few games and nothing else.",
            "The game server is Java on Elastic Beanstalk. The model runs inside it and picks a move in under a millisecond.",
            "Over 2,300 games, the trained bot beat the rule-based bot 82.9% of the time. Two rule-based bots split about 50/50."
        ],
        figures: [
            {
                label: "How it's built",
                image: "/assets/diagram-wilderchess-architecture.svg",
                description: "Spot instances play games and save them to S3. I train on my own GPU, then the game server loads the model."
            },
            {
                label: "Win rate",
                image: "/assets/portfolio_convergence_chart.png",
                description: "Left: the rule-based bot against itself, about 50/50. Right: the trained bot against the rule-based bot, 82.9% over 2,300 games."
            },
            {
                label: "Move time",
                image: "/assets/InferenceLatency.png",
                description: "How long the model takes to choose a move."
            },
            {
                label: "A game",
                video: "/assets/wilderchess-demo.webm",
                description: "The bot playing a real match."
            },
            {
                label: "Where it plays",
                image: "/assets/strategy_heatmap.png",
                description: "Which moves the bot prefers, compared with the rule-based bot."
            }
        ]
    }
];

const labs = [
    {
        title: "Needleman-Wunsch",
        link: "https://gabehouse.github.io/Needleman-Wunsch-Demo/",
        repo: "https://github.com/gabehouse/Needleman-Wunsch-Demo",
        summary: "DNA sequence alignment you can watch step by step.",
        technologies: ["React", "Jest", "GitHub Pages"],
        figures: [
            {
                label: "The demo",
                video: "/assets/nw-algo-demo.webm",
                description: "Filling in the grid, then tracing the best alignment back."
            }
        ]
    },
    {
        title: "3D Physics Demo",
        link: "https://gabehouse.github.io/js-physics-demo/",
        repo: "https://github.com/gabehouse/js-physics-demo",
        summary: "A little 3D court with bouncy balls and a pool of water. Runs in the browser.",
        technologies: ["Three.js", "WebGPU", "GitHub Pages"],
        figures: [
            {
                label: "The demo",
                video: "/assets/physics-lab-demo.mp4",
                description: "Hitting balls around and making waves."
            }
        ]
    }
];

const isVideo = (src) => src.endsWith('.webm') || src.endsWith('.mp4');

const Media = ({ item, className, lightbox = false }) => {
    const src = item.video || item.image;
    if (!src) return null;
    if (isVideo(src)) {
        return (
            <video
                src={src}
                className={className}
                autoPlay
                loop
                playsInline
                muted={!lightbox}
                controls={lightbox}
                onClick={(e) => lightbox && e.stopPropagation()}
            />
        );
    }
    return (
        <img
            src={src}
            alt={item.label}
            className={className}
            loading="lazy"
            onClick={(e) => lightbox && e.stopPropagation()}
        />
    );
};

const SourceLinks = ({ project }) => (
    <div className="project__links">
        {project.link && (
            <a href={project.link} target="_blank" rel="noreferrer" className="project__link project__link--live">
                Live ↗
            </a>
        )}
        {project.infraRepo && (
            <a href={project.infraRepo} target="_blank" rel="noreferrer" className="project__link">
                <Gear size={15} weight="bold" /> Terraform
            </a>
        )}
        {project.serviceRepo && (
            <a href={project.serviceRepo} target="_blank" rel="noreferrer" className="project__link">
                <GithubLogo size={15} weight="bold" /> App code
            </a>
        )}
        {project.repo && (
            <a href={project.repo} target="_blank" rel="noreferrer" className="project__link">
                <GithubLogo size={15} weight="bold" /> Source
            </a>
        )}
    </div>
);

const FiguresModal = ({ project, onClose }) => {
    const [zoomIndex, setZoomIndex] = useState(null);
    const total = project.figures.length;

    useEffect(() => {
        const handleKeyDown = (e) => {
            if (e.key === 'Escape') {
                if (zoomIndex !== null) setZoomIndex(null);
                else onClose();
                return;
            }
            if (zoomIndex === null) return;
            if (e.key === 'ArrowRight') setZoomIndex((i) => (i + 1) % total);
            if (e.key === 'ArrowLeft') setZoomIndex((i) => (i - 1 + total) % total);
        };
        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [zoomIndex, total, onClose]);

    useEffect(() => {
        const previousOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.body.style.overflow = previousOverflow;
        };
    }, []);

    const step = (delta) => (e) => {
        e.stopPropagation();
        setZoomIndex((i) => (i + delta + total) % total);
    };

    return createPortal(
        <>
            <div className="project-modal-overlay" onClick={onClose}>
                <div
                    className="project-modal"
                    role="dialog"
                    aria-modal="true"
                    aria-label={`${project.title} figures`}
                    onClick={(e) => e.stopPropagation()}
                >
                    <button type="button" className="project-modal__close" onClick={onClose} aria-label="Close">
                        <X size={18} weight="bold" />
                    </button>
                    <h2 className="project-modal__title">{project.title}</h2>
                    <div className="project-card__gallery">
                        {project.figures.map((item, i) => (
                            <div key={item.label} className="project-card__gallery-item">
                                <button
                                    type="button"
                                    className="project-card__gallery-thumb"
                                    onClick={() => setZoomIndex(i)}
                                >
                                    <Media item={item} />
                                </button>
                                <span>{item.label}</span>
                                <p>{item.description}</p>
                            </div>
                        ))}
                    </div>
                    <SourceLinks project={project} />
                </div>
            </div>

            {zoomIndex !== null && (
                <div className="project-lightbox" onClick={() => setZoomIndex(null)}>
                    {total > 1 && (
                        <button type="button" className="project-lightbox__nav project-lightbox__nav--prev" onClick={step(-1)}>‹</button>
                    )}
                    <div className="project-lightbox__content">
                        <Media item={project.figures[zoomIndex]} className="project-lightbox__media" lightbox />
                        <div className="project-lightbox__caption">
                            <h3>{project.figures[zoomIndex].label}</h3>
                            <p>{project.figures[zoomIndex].description}</p>
                            {total > 1 && <div className="project-lightbox__count">{zoomIndex + 1} / {total}</div>}
                        </div>
                    </div>
                    {total > 1 && (
                        <button type="button" className="project-lightbox__nav project-lightbox__nav--next" onClick={step(1)}>›</button>
                    )}
                </div>
            )}
        </>,
        document.body
    );
};

const System = ({ project, index }) => {
    const [open, setOpen] = useState(false);
    const cover = project.figures[0];

    return (
        <article className="system">
            <div className="system__text">
                <p className="system__index">{String(index + 1).padStart(2, '0')}</p>
                <h3 className="system__title">{project.title}</h3>
                <p className="system__summary">{project.summary}</p>
                <ul className="system__proof">
                    {project.proof.map((line) => (
                        <li key={line}><Markdown>{line}</Markdown></li>
                    ))}
                </ul>
                <div className="project-card__tags">
                    {project.technologies.map((tech) => (
                        <span key={tech} className="project-card__tag">{tech}</span>
                    ))}
                </div>
                <SourceLinks project={project} />
            </div>

            <button
                type="button"
                className="system__figure"
                onClick={() => setOpen(true)}
                aria-label={`Open ${project.title} diagrams and figures`}
            >
                <Media item={cover} className="system__figure-media" />
                <span className="system__figure-label">
                    {project.figures.length > 1 ? `Diagram and ${project.figures.length - 1} more` : 'Diagram'}
                </span>
            </button>

            {open && <FiguresModal project={project} onClose={() => setOpen(false)} />}
        </article>
    );
};

const Lab = ({ project }) => {
    const [open, setOpen] = useState(false);

    return (
        <article className="lab">
            <button type="button" className="lab__media" onClick={() => setOpen(true)} aria-label={`Open ${project.title} demo video`}>
                <Media item={project.figures[0]} className="lab__media-asset" />
            </button>
            <div className="lab__body">
                <h3 className="lab__title">{project.title}</h3>
                <p className="lab__summary">{project.summary}</p>
                <SourceLinks project={project} />
            </div>
            {open && <FiguresModal project={project} onClose={() => setOpen(false)} />}
        </article>
    );
};

const App = () => {
    const homeRef = useRef(null);
    const workRef = useRef(null);
    const contactRef = useRef(null);
    const [current, setCurrent] = useState("home");

    useEffect(() => {
        const sections = [
            [homeRef.current, "home"],
            [workRef.current, "work"],
            [contactRef.current, "contact"]
        ];
        const observer = new IntersectionObserver(
            (entries) => {
                entries.forEach((entry) => {
                    if (!entry.isIntersecting) return;
                    const match = sections.find(([el]) => el === entry.target);
                    if (match) setCurrent(match[1]);
                });
            },
            { rootMargin: '-20% 0% -70% 0%', threshold: 0 }
        );
        sections.forEach(([el]) => el && observer.observe(el));
        return () => observer.disconnect();
    }, []);

    const scrollTo = (ref) => () => ref.current?.scrollIntoView({ behavior: 'smooth' });

    const navItems = [
        ["home", "Home", homeRef],
        ["work", "Work", workRef],
        ["contact", "Contact", contactRef]
    ];

    return (
        <div className="app-container">
            <nav className="site-nav">
                {navItems.map(([id, label, ref]) => (
                    <button
                        key={id}
                        type="button"
                        className={`site-nav__item${current === id ? ' is-active' : ''}`}
                        onClick={scrollTo(ref)}
                    >
                        {label}
                    </button>
                ))}
            </nav>

            <header ref={homeRef} className="home">
                <p className="home__eyebrow">Cloud engineer</p>
                <h1 className="home__name">Gabriel House</h1>
                <p className="home__meta">Computer Science, University of Waterloo</p>
                <p className="home__lede">
                    I build things on AWS, mostly with Terraform. Here are three of them. All three are deployed, and the code is on GitHub.
                </p>
                <div className="home__actions">
                    <button type="button" className="home__button home__button--primary" onClick={scrollTo(workRef)}>
                        See the work
                    </button>
                    <a className="home__button" href={GITHUB} target="_blank" rel="noreferrer">
                        <GithubLogo size={16} weight="bold" /> GitHub
                    </a>
                    <a className="home__button" href={`mailto:${EMAIL}`}>
                        <Envelope size={16} weight="bold" /> {EMAIL}
                    </a>
                </div>
            </header>

            <main ref={workRef} className="work">
                <h2 className="work__heading">Projects</h2>
                <div className="work__systems">
                    {systems.map((project, i) => (
                        <System key={project.title} project={project} index={i} />
                    ))}
                </div>

                <h2 className="work__heading work__heading--labs">Side projects</h2>
                <p className="work__note">Just for fun. No AWS in these.</p>
                <div className="work__labs">
                    {labs.map((project) => (
                        <Lab key={project.title} project={project} />
                    ))}
                </div>
            </main>

            <footer ref={contactRef} className="contact">
                <h2 className="contact__title">Contact</h2>
                <p className="contact__lede">Open to cloud and backend software engineering opportunities. Let's connect.</p>
                <div className="contact__links">
                    <a className="contact__link" href={`mailto:${EMAIL}`}>
                        <Envelope size={20} /> {EMAIL}
                    </a>
                    <a className="contact__link" href={LINKEDIN} target="_blank" rel="noreferrer">
                        <LinkedinLogo size={20} /> linkedin.com/in/gabriel-house
                    </a>
                    <a className="contact__link" href={GITHUB} target="_blank" rel="noreferrer">
                        <GithubLogo size={20} /> github.com/gabehouse
                    </a>
                </div>
            </footer>
        </div>
    );
};

export default App;
