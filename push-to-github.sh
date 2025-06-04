#!/bin/bash

echo "🚀 MazeWarden 3D - Push to GitHub"
echo "================================="
echo ""
echo "📡 Configurazione Git..."

# Configura Git con i tuoi dati
git config user.name "Marcone1983"
git config user.email "whitecasteddu@gmail.com"

# Rimuovi remote esistente se presente
git remote remove origin 2>/dev/null

# Aggiungi il remote GitHub
echo "📡 Collegamento a GitHub..."
git remote add origin https://github.com/Marcone1983/MazeWarder.git

# Force push (sovrascrive tutto)
echo "💪 Force push del codice (sovrascrive README)..."
git push -u origin master --force

echo ""
echo "✅ FATTO! MazeWarden 3D è ora su GitHub!"
echo "🔗 URL: https://github.com/Marcone1983/MazeWarder"
echo ""
echo "📱 Per buildare l'APK localmente:"
echo "   ./build-apk.sh"
echo ""
echo "⚠️  NOTA: GitHub non ha CI/CD automatico come GitLab"
echo "   Dovrai buildare l'APK manualmente con lo script sopra"