# Backup & Disaster Recovery Configuration

## Backup Storage Bucket

- **Backup Bucket / Prefix**: `proud-gray-gecko-backups-vkgy5/`
- **Storage Provider**: Cloudflare R2 (S3-compatible object storage)
- **Primary CDN / Storage Endpoint**: `https://f6d1d15e6f0b37b4b8fcad3c41a7922d.r2.cloudflarestorage.com`

## Environment Variables

Configure the following variables in your production environment (Sevalla / Docker):

```env
STORAGE_BASE_URL=https://f6d1d15e6f0b37b4b8fcad3c41a7922d.r2.cloudflarestorage.com
BACKUP_BUCKET=proud-gray-gecko-backups-vkgy5/
```

## Automated Backups

For database snapshots and media assets backup routines:
1. Database exports (PostgreSQL `pg_dump`) should be compressed (`.tar.gz`) and uploaded to `proud-gray-gecko-backups-vkgy5/database/`.
2. Media and upload assets should be synced periodically to `proud-gray-gecko-backups-vkgy5/assets/`.
