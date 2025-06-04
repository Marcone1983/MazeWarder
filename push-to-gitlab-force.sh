#!/bin/bash

echo "🚀 MazeWarden 3D - Force Push to GitLab"
echo "======================================="
echo ""
echo "⚠️  Questo script sovrascriverà il repository GitLab esistente"
echo "   (cancella README e sostituisce con il nostro progetto)"
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

# Force push (sovrascrive tutto)
echo "💪 Force push del codice (sovrascrive README)..."
git push -u origin master --force

echo ""
echo "✅ FATTO! Il tuo gioco ha sostituito tutto su GitLab!"
echo "🔗 URL: $GITLAB_URL"
echo ""
echo "📱 Per buildare l'APK:"
echo "   ./build-apk.sh"