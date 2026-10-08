# MovieKnight User API

Spring Boot REST API for email-first account enrollment and password login.

## Enrollment and login flow

1. Submit an email address to `POST /api/v1/auth/email`.
   - Existing account: the response selects the login flow.
   - New valid email: the API emails a six-digit code that expires in five minutes.
   - Invalid email: the API returns a field-specific `400` validation error.
   - A new code can be requested once per email every 60 seconds.
2. Submit the email and code to `POST /api/v1/auth/verify-email`. A valid code returns a short-lived registration token (10 minutes).
3. Submit the name, password, confirmation, and registration token to `POST /api/v1/users`. The verified email is bound to the token and is not accepted again in the signup request. Passwords are stored as BCrypt hashes.
4. Existing users log in at `POST /api/v1/auth/login`. The client carries the email from the first step and sends it with the password; the login screen only needs to ask for the password.

Email selection intentionally distinguishes registered and unregistered addresses to support this flow. Verification codes are stored as password hashes, expire after five minutes, and allow at most five guesses.

### Check email

```http
POST /api/v1/auth/email
Content-Type: application/json
```

```json
{"emailId":"alex@example.com"}
```

Existing account response:

```json
{"statusCode":200,"nextStep":"LOGIN","message":"Enter your password to log in."}
```

New email response:

```json
{"statusCode":200,"nextStep":"VERIFY_EMAIL","message":"A verification code was sent to your email."}
```

### Verify email

```http
POST /api/v1/auth/verify-email
Content-Type: application/json
```

```json
{"emailId":"alex@example.com","passcode":"123456"}
```

On success, the response contains a `registrationToken`; pass it to the final signup request. Do not log or share this token.

### Complete signup

```http
POST /api/v1/users
Content-Type: application/json
```

```json
{
  "name": "Alex Doe",
  "password": "correct-horse-7",
  "confirmPassword": "correct-horse-7",
  "registrationToken": "TOKEN_FROM_EMAIL_VERIFICATION_RESPONSE"
}
```

Successful signup returns `201 Created` with a welcome message. Field validation errors return a `statusCode` and an `errors` object; duplicate email returns `409 Conflict`.

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{"emailId":"alex@example.com","password":"correct-horse-7"}
```

Incorrect credentials return the same generic reason whether the email or password is wrong. Login currently verifies credentials but does not issue a session or access token; add that before exposing protected account APIs.

## Database

The application uses PostgreSQL. For local development, create a database named `movienight` and configure `DB_USERNAME` and `DB_PASSWORD`. To connect to a hosted PostgreSQL database, set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Hibernate creates/updates tables on startup.

If upgrading an existing database from the earlier phone-based user schema, remove the obsolete column once:

```sql
ALTER TABLE public.users DROP COLUMN IF EXISTS phone_number;
```

## Email configuration

For Gmail, use Google's SMTP server with the app password for the same account. Enable 2-Step Verification and generate an app password in your Google Account security settings. Configure these environment variables in IntelliJ's run configuration, or set them in PowerShell and start the app from that same PowerShell session:

```powershell
$env:MAIL_HOST = "smtp.gmail.com"
$env:MAIL_PORT = "587"
$env:MAIL_USERNAME = "your.address@gmail.com"
$env:MAIL_PASSWORD = "YOUR-16-CHARACTER-APP-PASSWORD"
$env:MAIL_FROM = "your.address@gmail.com"
$env:MAIL_SMTP_AUTH = "true"
$env:MAIL_SMTP_STARTTLS = "true"
```

`MAIL_FROM` should be the authenticated Gmail address unless you have configured a permitted alias. Paste the app password without spaces. The application defaults to `smtp.gmail.com:587` with SMTP authentication and STARTTLS enabled, so `MAIL_HOST`, `MAIL_PORT`, `MAIL_SMTP_AUTH`, and `MAIL_SMTP_STARTTLS` can be omitted for Gmail. Keep `MAIL_PASSWORD` out of source control. If delivery still fails, verify the username and app password belong to the same account, confirm that the account allows app passwords, and inspect the application logs for the SMTP error. Delivery failures return `503 Service Unavailable`.

Start the API from the project root with `mvn spring-boot:run`.
