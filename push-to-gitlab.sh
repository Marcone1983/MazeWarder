#!/bin/bash

echo "🚀 MazeWarden 3D - Push to GitLab"
echo "=================================="
echo ""
echo "⚠️  PRIMA DI CONTINUARE:"
echo "1. Vai su https://gitlab.com"
echo "2. Crea un nuovo progetto chiamato 'mazewarden3d'"
echo "3. NON inizializzare con README"
echo "4. Copia l'URL del repository (es: https://gitlab.com/tuousername/mazewarden3d.git)"
echo ""
read -p "Hai creato il progetto su GitLab? (s/n): " risposta

if [ "$risposta" != "s" ]; then
    echo "❌ Crea prima il progetto su GitLab!"
    exit 1
fi

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

# Push del codice
echo "📤 Upload del codice..."
git push -u origin master

echo ""
echo "✅ FATTO! Il tuo gioco è ora su GitLab!"
echo "🔗 URL: $GITLAB_URL"
echo ""
echo "📱 Per buildare l'APK:"
echo "   ./build-apk.sh"