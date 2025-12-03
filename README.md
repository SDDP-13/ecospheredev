# EcoSphere — Project Setup Repository



*COMP2300 — Group Project*



This repository contains the initial setup for the EcoSphere application. It serves as the foundation for future development and includes early structural folders, assets, and placeholders required to begin implementing the system.



---



## Overview



EcoSphere is a mobile application concept designed to encourage sustainable household energy behaviour. The system combines scheduling tools, reminders, and a light gamified progression model where users complete real-world energy-saving tasks to earn in-game rewards.



This repository currently contains the basic structure required for the upcoming development phase.



---

# **Git Setup Guide**
**Note:** If you only use **one GitHub account**, follow the **Single Account** steps.  
If you use **multiple GitHub accounts**, follow the **Multi-Account (Required)** steps.  

**Recommended:** Use **Git Bash** on Windows

---

## 1. Create a Folder for the Project
This creates a workspace where the project will be stored.
```sh
mkdir <your-folder-name>
cd <your-folder-name>
```

Example:
```sh
mkdir workspace
cd workspace
```

---

## 2. Generate SSH Key

**Single Account**
```sh
ssh-keygen -t ed25519 -C "username@soton.ac.uk"
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519
cat ~/.ssh/id_ed25519.pub
```

**Multi-Account (generate Uni key separately)**
```sh
ssh-keygen -t ed25519 -C "username@soton.ac.uk" -f ~/.ssh/id_ed25519_uni
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519_uni
cat ~/.ssh/id_ed25519_uni.pub
```

Add the key to GitHub → Settings → SSH and GPG keys → New SSH key

---

## 3. SSH Config (Required for Multi-Account)

**Open config file with Notepad**
```sh
notepad ~/.ssh/config
```

Add the following:
```txt
# Single / Personal GitHub account
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/id_ed25519

# Uni GitHub account
Host github.com-uni
    HostName github.com
    User git
    IdentityFile ~/.ssh/id_ed25519_uni
```

Save and close Notepad.

---


## 4. Test SSH Connection

This checks whether your SSH key is correctly linked to GitHub.

```sh
ssh -T git@github.com
```

**Success Message**

If everything is set up correctly, you will see:

```
Hi username! You've successfully authenticated, but GitHub does not provide shell access.
```

(Your GitHub username will appear in place of `username`.)


---


## 5. Clone Repository (SSH)

**Single Account**
```sh
git clone git@github.com:SDDP-13/ecospheredev.git
cd ecospheredev
```

**Multi-Account**
```sh
git clone git@github.com-uni:SDDP-13/ecospheredev.git
cd ecospheredev
```

---


## 6. Configure Remote

**Single Account**

Use this if you only have one GitHub account on your computer.  

```sh
git remote -v                                                           # Check current remotes
git remote add origin git@github.com:SDDP-13/ecospheredev.git           # Add remote if empty
git remote set-url origin git@github.com:SDDP-13/ecospheredev.git       # Update remote URL
```

---

**Multi-Account**

Use this if you set up `.ssh/config` with a separate **github.com-uni** host.  

```sh
git remote -v                                                           # Check current remotes
git remote add origin git@github.com-uni:SDDP-13/ecospheredev.git       # Add remote if empty
git remote set-url origin git@github.com-uni:SDDP-13/ecospheredev.git   # Update remote URL
```

---

## 7. Switch to Main Branch

This ensures you are working on the main branch of the project.
```sh
git checkout main
```

---


## 8. Stage, Commit, Push

This saves your changes and uploads them to the repository.
```sh
git add .
git commit -m "Message"
git push -u origin main
```

---

## 9. Pull Latest Changes

This updates your local files with the newest version from the repository.
```sh
git pull origin main
```

---


## 10. Modify .gitignore

This prevents unwanted local files from being uploaded to the repository.

Edit the `.gitignore` file:

```sh
notepad .gitignore
```

Add the folder you want to ignore (example: ignore Visual Studio’s `.vs` folder):

```
.vs/
```

Save and close.

---


## Repository Structure

* **assets**

  * **ui**
    Images, icons, and visual references

* **common**

  * **utils**
    Shared utilities or helper modules

* **create**

  * **generators**
    Scripts or templates for building app components

* **runs**

  * **testing**
    Runtime logs, prototype runs, or test outputs

* **README.md**
  Project overview and documentation


---



## Planned Core Features



This repository will eventually support the implementation of the following foundational features:



* User login and account setup

* Reminder and scheduling system for household devices

* Daily task system with progress tracking

* Gamified resource and structure progression

* Dashboard displaying key user metrics

* Build mode for placing and managing in-game structures

* Solar system navigation representing user progress

* Settings for personalisation and notification control



These form the baseline scope for the first development milestone.



---



## Development Notes



Final technology choices are still to be confirmed. This repository currently acts as the starting point for organising shared code, assets, and development processes. As the project progresses, new directories, modules, and implementation files will be added to support the core features.



---



## Contributors



Group 13 – COMP2300



* Ebaa Al Hinai

* Daniel Krupin

* Chun Hei Hadrian Lee

* Michael Wye

* Yuma Rai



---



## Licence



This project is for academic use as part of the COMP2300 Software Design and Development module.



