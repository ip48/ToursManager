# GitHub SSH with WSL + VS Code Dev Containers

This repo is set up to use your WSL SSH keys inside the Dev Container without hardcoding usernames.

## TL;DR
- Generate your key on Windows (PowerShell)
- Copy it into WSL and fix permissions
- Rebuild the Dev Container
- Test SSH and push from the Dev Container terminal

## 1) Generate key on Windows (PowerShell)

```powershell
ssh-keygen -t ed25519 -C "your-email@example.com"
# Accept the default path: C:\Users\<You>\.ssh\id_ed25519
```

## 2) Copy key into WSL (Ubuntu) and set strict permissions
Run these in your WSL terminal:

```bash
sudo rm -rf ~/.ssh
sudo install -d -m 700 -o "$USER" -g "$USER" ~/.ssh
cp -f /mnt/c/Users/<You>/.ssh/id_ed25519* ~/.ssh/
chmod 600 ~/.ssh/id_ed25519
chmod 644 ~/.ssh/id_ed25519.pub
ls -la ~/.ssh
```

Why: OpenSSH requires `~/.ssh` to be `700`, private key `600`, public key `644`.

## 3) Rebuild the Dev Container
In VS Code:
- Command Palette → "Dev Containers: Rebuild Container"

This repo’s `devcontainer.json` binds `${HOME}/.ssh` (from WSL) to `/home/vscode/.ssh` in the container.

## 4) Verify inside the Dev Container
Use the VS Code terminal that shows a prompt like `vscode@...:/workspaces/ToursManager$`:

```bash
ls -la ~/.ssh/
ssh-keygen -lf ~/.ssh/id_ed25519.pub -E sha256
```

## 5) Add the public key to GitHub
Copy `~/.ssh/id_ed25519.pub` and paste it at GitHub → Settings → SSH and GPG keys → New SSH key.

## 6) Test SSH and push (inside the Dev Container)

```bash
ssh -T git@github.com   # Accept the host key on first connect

# Set remote and push (example)
git remote add origin git@github.com:<your-username>/ToursManager.git
git push -u origin main
```

## Notes
- No usernames are hardcoded: `${HOME}` resolves to your WSL user’s home.
- Prefer this (WSL-hosted keys) on Windows + WSL2. It’s the least fragile.
- If you want agent forwarding (no keys mounted), we can switch to that later.
