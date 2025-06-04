#!/bin/bash

echo "🚀 MazeWarden 3D - Merge Push to GitLab"
echo "======================================="
echo ""
echo "ℹ️  Questo script mantiene il README e aggiunge il nostro progetto"
echo ""
read -p "Incolla l'URL del tuo repository GitLab: " GITLAB_URL

if [ -z "$GITLAB_URL" ]; then
    echo "❌ URL non valido!"
    exit 1
fi

echo ""
read -p "Inserisci il tuo username GitLab: " GITLAB_USER
read -p "Inserisci la tua email GitLab: " GITLAB_EMAIL

# Configura Git
git config user.name "$GITLAB_USER"
git config user.email "$GITLAB_EMAIL"

# Aggiungi il remote
echo "📡 Collegamento a GitLab..."
git remote add origin $GITLAB_URL

# Pull del README esistente
echo "📥 Scaricamento README esistente..."
git pull origin master --allow-unrelated-histories

# Push del nostro codice
echo "📤 Upload del codice..."
git push -u origin master

echo ""
echo "✅ FATTO! Il tuo gioco è ora su GitLab con README originale!"
echo "🔗 URL: $GITLAB_URL"
echo ""
echo "📱 Per buildare l'APK:"
echo "   ./build-apk.sh"