# 📨 Service de Notifications avec Kafka

## 🎯 Vue d'ensemble

Ce projet implémente un système de notifications en temps réel pour alerter les passagers sur les retards et annulations de bus via email et SMS, en utilisant Apache Kafka comme système de messagerie.

## 🏗️ Architecture

```
┌─────────────────────┐
│  Tracking Service   │  (Service de Géolocalisation)
│  (Producer Kafka)   │
└──────────┬──────────┘
           │ Envoie événements
           ▼
┌─────────────────────┐
│       KAFKA         │  Topics:
│   Message Broker    │  - bus-delays
│                     │  - bus-cancellations
└──────────┬──────────┘  - bus-alerts
           │
           │ Consomme événements
           ▼
┌─────────────────────┐
│ Notification Service│
│  (Consumer Kafka)   │
└──────────┬──────────┘
           │
           ├──────────┐
           ▼          ▼
     📧 Email    📱 SMS
```

### Flux de données

1. **Tracking Service** détecte un retard/annulation
2. Crée un événement `BusEventNotification`
3. **Envoie** l'événement dans un **topic Kafka**
4. **Kafka** stocke le message et le distribue
5. **Notification Service** **reçoit** l'événement automatiquement
6. Envoie des **emails** et/ou **SMS** aux passagers
7. Sauvegarde l'historique dans **MongoDB**

## 📚 Comprendre Kafka - Pour Débutants

### Qu'est-ce que Kafka ?

**Kafka** est comme un **bureau de poste numérique** :
- Les services peuvent **envoyer** des messages (Producers)
- D'autres services peuvent **recevoir** ces messages (Consumers)
- Les messages sont organisés en **topics** (comme des boîtes aux lettres)

### Concepts clés

#### 1. **Topic** (Sujet)
Un canal de communication. Exemples dans notre projet :
- `bus-delays` : pour les retards
- `bus-cancellations` : pour les annulations
- `bus-alerts` : pour les alertes générales

#### 2. **Producer** (Producteur)
Un service qui **envoie** des messages dans un topic.
- Dans notre cas : **Tracking Service** (géolocalisation)

#### 3. **Consumer** (Consommateur)
Un service qui **lit** les messages depuis un topic.
- Dans notre cas : **Notification Service**

#### 4. **Message**
Les données envoyées (format JSON dans notre cas).
Exemple :
```json
{
  "eventId": "abc-123",
  "eventType": "DELAY",
  "busNumber": "42",
  "routeNumber": "A",
  "delayMinutes": 15,
  "message": "Retard dû à un accident"
}
```

### Pourquoi utiliser Kafka ?

✅ **Asynchrone** : Le tracking service n'attend pas que les emails soient envoyés
✅ **Scalable** : Peut gérer des millions de messages
✅ **Résilient** : Les messages sont persistés (pas de perte)
✅ **Découplé** : Les services ne se connaissent pas directement

## 🚀 Démarrage

### 1. Configuration des variables d'environnement

Créez/modifiez le fichier `.env` à la racine du projet :

```bash
# Email Configuration (Gmail example)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=votre-email@gmail.com
SMTP_PASSWORD=votre-mot-de-passe-app
SMTP_FROM=noreply@transport.com
EMAIL_NOTIFICATIONS_ENABLED=true

# SMS Configuration (Twilio - optionnel)
TWILIO_ACCOUNT_SID=votre-account-sid
TWILIO_AUTH_TOKEN=votre-auth-token
TWILIO_PHONE_NUMBER=+1234567890
SMS_NOTIFICATIONS_ENABLED=false

# JWT
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000
```

### 2. Démarrer les services

```bash
# Démarrer tous les services avec Docker Compose
docker-compose up -d

# Vérifier que tous les services sont démarrés
docker-compose ps

# Voir les logs du service de notifications
docker-compose logs -f notification-service

# Voir les logs du service de tracking
docker-compose logs -f tracking-service
```

### 3. Accéder aux interfaces

- **Kafka UI** : http://localhost:8090
  - Visualiser les topics, messages, consumers
- **Eureka** : http://localhost:8761
  - Voir tous les services enregistrés
- **Notification Service** : http://localhost:8095
- **Tracking Service** : http://localhost:8092

## 🧪 Tester le système

### Test 1 : Signaler un retard (via Tracking Service)

```bash
# Créer d'abord un bus (si pas déjà fait)
curl -X POST http://localhost:8092/api/buses \
  -H "Content-Type: application/json" \
  -d '{
    "busNumber": "42",
    "routeNumber": "A",
    "capacity": 50,
    "type": "STANDARD"
  }'

# Signaler un retard de 20 minutes
curl -X POST http://localhost:8092/api/bus-events/delay \
  -H "Content-Type: application/json" \
  -d '{
    "busId": "RÉCUPÉRER_ID_DU_BUS_CRÉÉ",
    "delayMinutes": 20,
    "message": "Retard dû à un accident sur l autoroute"
  }'
```

**Ce qui se passe :**
1. Le Tracking Service envoie un message dans le topic `bus-delays`
2. Kafka stocke le message
3. Le Notification Service reçoit le message
4. Un email est envoyé (et SMS si activé)
5. L'historique est sauvegardé dans MongoDB

### Test 2 : Vérifier dans Kafka UI

1. Ouvrez http://localhost:8090
2. Cliquez sur le cluster "local"
3. Allez dans "Topics"
4. Cliquez sur `bus-delays`
5. Allez dans "Messages"
6. Vous verrez votre message !

### Test 3 : Consulter l'historique des notifications

```bash
# Voir toutes les notifications envoyées
curl http://localhost:8095/api/notifications/history

# Voir les notifications récentes
curl http://localhost:8095/api/notifications/recent

# Voir les statistiques
curl http://localhost:8095/api/notifications/stats
```

### Test 4 : Autres types d'événements

#### Annulation
```bash
curl -X POST http://localhost:8092/api/bus-events/cancellation \
  -H "Content-Type: application/json" \
  -d '{
    "busId": "VOTRE_BUS_ID",
    "reason": "Panne technique"
  }'
```

#### Panne
```bash
curl -X POST http://localhost:8092/api/bus-events/breakdown \
  -H "Content-Type: application/json" \
  -d '{
    "busId": "VOTRE_BUS_ID",
    "description": "Problème moteur"
  }'
```

#### Alerte trafic
```bash
curl -X POST http://localhost:8092/api/bus-events/traffic-alert \
  -H "Content-Type: application/json" \
  -d '{
    "routeNumber": "A",
    "location": "Avenue des Champs-Élysées",
    "description": "Embouteillages importants",
    "latitude": 48.8698,
    "longitude": 2.3078
  }'
```

## 📊 Monitoring

### Kafka UI (http://localhost:8090)

**Topics** :
- Voir tous les topics créés
- Nombre de messages dans chaque topic
- Taille des partitions

**Consumers** :
- Voir les consumers actifs
- Lag (retard de consommation)
- Offsets (position de lecture)

**Messages** :
- Lire les messages dans un topic
- Voir le contenu JSON
- Filtrer par partition/offset

### Logs des services

```bash
# Logs en temps réel - Notification Service
docker-compose logs -f notification-service

# Rechercher des erreurs
docker-compose logs notification-service | grep ERROR

# Logs en temps réel - Tracking Service
docker-compose logs -f tracking-service
```

Vous verrez des logs comme :
```
📤 Publishing DELAY event for bus 42 (Route A): 20 minutes
✅ Event sent successfully to topic 'bus-delays' | Partition: 0 | Offset: 5
📨 [KAFKA CONSUMER] Received delay event from partition 0 offset 5
📧 Sending email to: passenger@transport.com
✅ Email sent successfully
```

## 📁 Structure du projet

```
services/
├── notification-service/          # 🔔 Service de notifications (Consumer)
│   ├── src/main/java/com/transport/notification/
│   │   ├── consumer/
│   │   │   └── BusEventConsumer.java     # ⭐ Consumer Kafka (écoute les topics)
│   │   ├── service/
│   │   │   ├── EmailService.java         # 📧 Envoi d'emails
│   │   │   └── SmsService.java           # 📱 Envoi de SMS
│   │   ├── config/
│   │   │   ├── KafkaConsumerConfig.java  # ⚙️ Configuration Kafka Consumer
│   │   │   └── KafkaTopicConfig.java     # 📋 Définition des topics
│   │   ├── dto/
│   │   │   └── BusEventNotification.java # 📦 Format des messages
│   │   └── model/
│   │       └── NotificationHistory.java  # 💾 Historique MongoDB
│   └── Dockerfile
│
└── tracking-service/              # 📍 Service de géolocalisation (Producer)
    ├── src/main/java/com/transport/tracking/
    │   ├── kafka/
    │   │   ├── BusEventProducer.java      # ⭐ Producer Kafka (envoie vers topics)
    │   │   ├── KafkaProducerConfig.java   # ⚙️ Configuration Kafka Producer
    │   │   └── BusEventNotification.java  # 📦 Format des messages
    │   └── controller/
    │       └── BusEventController.java    # 🌐 API REST pour tester
    └── Dockerfile
```

## 🔧 Configuration Kafka

### Topics créés automatiquement

| Topic | Partitions | Utilisation |
|-------|------------|-------------|
| `bus-delays` | 3 | Retards de bus |
| `bus-cancellations` | 3 | Annulations |
| `bus-alerts` | 3 | Alertes générales |
| `bus-location-updates` | 5 | Positions GPS (optionnel) |

### Consumer Group

- **Group ID** : `notification-service-group`
- **Auto-commit** : Activé (1000ms)
- **Offset reset** : earliest (lit depuis le début)

### Producer Config

- **Acks** : all (confirmation de tous les brokers)
- **Retries** : 3 (réessaye 3 fois en cas d'échec)
- **Serialization** : JSON

## 📧 Configuration Email

### Gmail

1. Activer la validation en 2 étapes
2. Générer un "Mot de passe d'application"
3. Utiliser ce mot de passe dans `SMTP_PASSWORD`

### Autres fournisseurs

| Fournisseur | SMTP_HOST | SMTP_PORT |
|-------------|-----------|-----------|
| Gmail | smtp.gmail.com | 587 |
| Outlook | smtp-mail.outlook.com | 587 |
| Yahoo | smtp.mail.yahoo.com | 587 |
| SendGrid | smtp.sendgrid.net | 587 |

## 📱 Configuration SMS (Twilio)

1. Créer un compte sur https://www.twilio.com
2. Obtenir un numéro de téléphone Twilio
3. Copier `Account SID` et `Auth Token`
4. Configurer dans `.env`

**Mode développement** : Si Twilio n'est pas configuré, les SMS sont simulés dans les logs.

## 🐛 Dépannage

### Le consumer ne reçoit pas les messages

```bash
# Vérifier que Kafka est démarré
docker-compose ps kafka

# Vérifier les topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9093

# Voir les messages dans un topic
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9093 \
  --topic bus-delays \
  --from-beginning
```

### Les emails ne sont pas envoyés

1. Vérifier les logs : `docker-compose logs notification-service | grep EMAIL`
2. Vérifier la config SMTP dans `.env`
3. Vérifier que `EMAIL_NOTIFICATIONS_ENABLED=true`

### Kafka UI ne démarre pas

```bash
# Redémarrer Kafka UI
docker-compose restart kafka-ui

# Vérifier les logs
docker-compose logs kafka-ui
```

## 🎓 Pour aller plus loin

### Exercices pratiques

1. **Modifier le seuil de retard** : Changer la logique pour envoyer SMS seulement si retard > 30 min
2. **Ajouter un nouveau type d'événement** : Créer `SCHEDULE_CHANGE`
3. **Implémenter un retry** : Réessayer l'envoi d'email si échec
4. **Ajouter des templates HTML** : Personnaliser les emails

### Concepts avancés à explorer

- **Partitionnement** : Distribuer les messages sur plusieurs partitions
- **Consumer Groups** : Plusieurs instances du service de notifications
- **Dead Letter Queue** : Gérer les messages qui échouent
- **Schema Registry** : Valider le format des messages
- **Kafka Streams** : Traiter les flux en temps réel

## 📖 Ressources

- [Documentation Kafka](https://kafka.apache.org/documentation/)
- [Spring Kafka](https://spring.io/projects/spring-kafka)
- [Twilio SMS](https://www.twilio.com/docs/sms)
- [Spring Mail](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#mail)

## 🤝 Support

Pour toute question sur Kafka ou ce projet, consultez :
- Les logs : `docker-compose logs -f`
- Kafka UI : http://localhost:8090
- MongoDB (historique) : http://localhost:8083

---

**Bonne découverte de Kafka ! 🚀**
