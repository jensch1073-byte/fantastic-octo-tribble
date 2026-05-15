# fantastic-octo-tribble

## exFAT Hilfsprogramm

Diese Repository enthält ein modularisiertes Groovy-Skript zur Installation, Montage und optionalen Eintragung von exFAT-Geräten in `/etc/fstab`.

### Benutzung

```bash
groovy exfat.groovy install
groovy exfat.groovy mount /dev/sdb1 /mnt/exfat
groovy exfat.groovy unmount /mnt/exfat
groovy exfat.groovy fstab /dev/sdb1 /mnt/exfat
```

Alternativ kannst du den Shell-Wrapper `exfat.sh` verwenden:

```bash
./exfat.sh install
./exfat.sh mount /dev/sdb1 /mnt/exfat
./exfat.sh unmount /mnt/exfat
./exfat.sh fstab /dev/sdb1 /mnt/exfat
```

Wenn keine Parameter angegeben werden, fragt das Skript interaktiv nach Gerät und Mount-Punkt.
