#!/usr/bin/env groovy

import java.nio.file.Files
import java.nio.file.Paths

class ExfatGroovy {
    static void main(String[] args) {
        def command = args.length ? args[0].toLowerCase() : 'help'
        switch (command) {
            case 'install':
                ensureExfatPackages()
                break
            case 'mount':
                mountInteractive(args)
                break
            case 'unmount':
                requireArguments(args, 2, 'unmount <mount-point>')
                unmount(args[1])
                break
            case 'fstab':
                requireArguments(args, 3, 'fstab <device> <mount-point>')
                addFstabEntry(args[1], args[2])
                break
            case 'help':
            default:
                printHelp()
                break
        }
    }

    static void printHelp() {
        println '''
ExfatGroovy - modulares exFAT-Hilfsprogramm

Verwendung:
  groovy exfat.groovy install
  groovy exfat.groovy mount [device] [mount-point]
  groovy exfat.groovy unmount <mount-point>
  groovy exfat.groovy fstab <device> <mount-point>

Wenn keine Device- oder Mount-Point-Parameter angegeben sind, fragt das Skript interaktiv nach ihnen.

Beispiele:
  groovy exfat.groovy install
  groovy exfat.groovy mount /dev/sdb1 /mnt/exfat
  groovy exfat.groovy unmount /mnt/exfat
  groovy exfat.groovy fstab /dev/sdb1 /mnt/exfat
'''
    }

    static void requireArguments(String[] args, int count, String usage) {
        if (args.length < count) {
            println "Fehler: Ungültige Anzahl an Argumenten. Erwartet: ${usage}."
            System.exit(1)
        }
    }

    static void mountInteractive(String[] args) {
        def device = args.length > 1 ? args[1] : ask('Device (z. B. /dev/sdb1)')
        def mountPoint = args.length > 2 ? args[2] : ask('Mount-Point (z. B. /mnt/exfat)')

        ensureExfatPackages()
        createMountPoint(mountPoint)
        mountDevice(device, mountPoint)

        if (confirm('Möchtest du den Eintrag in /etc/fstab hinzufügen? (y/n)')) {
            addFstabEntry(device, mountPoint)
        }
    }

    static void ensureExfatPackages() {
        if (!Files.exists(Paths.get('/usr/sbin/mkfs.exfat')) && !Files.exists(Paths.get('/usr/bin/mount.exfat')) && !Files.exists(Paths.get('/sbin/mount.exfat'))) {
            println 'exFAT-Pakete werden installiert...'
            if (!commandExists('apt')) {
                println 'Fehler: apt wurde nicht gefunden. Installiere exfat-fuse und exfat-utils manuell.'
                System.exit(1)
            }
            runCommand('sudo apt update')
            runCommand('sudo apt install -y exfat-fuse exfat-utils')
        } else {
            println 'exFAT-Pakete sind bereits installiert.'
        }
    }

    static void createMountPoint(String mountPoint) {
        def path = Paths.get(mountPoint)
        if (Files.exists(path)) {
            println "Mount-Point existiert bereits: ${mountPoint}"
            return
        }
        println "Erstelle Mount-Point: ${mountPoint}"
        runCommand("sudo mkdir -p ${mountPoint}")
    }

    static void mountDevice(String device, String mountPoint) {
        println "Mounten von ${device} nach ${mountPoint}..."
        runCommand("sudo mount -t exfat ${device} ${mountPoint}")
        println 'Mount erfolgreich.'
    }

    static void unmount(String mountPoint) {
        println "Hänge ${mountPoint} aus..."
        runCommand("sudo umount ${mountPoint}")
        println 'Aushängen erfolgreich.'
    }

    static void addFstabEntry(String device, String mountPoint) {
        println "Füge /etc/fstab Eintrag hinzu für ${device} -> ${mountPoint}"
        def entry = "${device} ${mountPoint} exfat defaults,nofail 0 0"
        def tempFile = Files.createTempFile('exfat-fstab', '.tmp')
        runCommand("sudo cp /etc/fstab ${tempFile}")
        runCommand("sudo bash -lc 'echo \"${entry}\" >> ${tempFile}'")
        runCommand("sudo cp ${tempFile} /etc/fstab")
        Files.deleteIfExists(tempFile)
        println 'Eintrag hinzugefügt. Bitte überprüfe /etc/fstab manuell auf Korrektheit.'
    }

    static boolean commandExists(String command) {
        try {
            def proc = ['bash', '-lc', "command -v ${command} >/dev/null 2>&1"].execute()
            proc.waitFor()
            return proc.exitValue() == 0
        } catch (Exception ignored) {
            return false
        }
    }

    static boolean confirm(String question) {
        def answer = ask(question, 'n').toLowerCase()
        return answer == 'y' || answer == 'j'
    }

    static String ask(String prompt, String defaultValue = '') {
        print "${prompt}${defaultValue ? ' [' + defaultValue + ']' : ''}: "
        def input = System.console()?.readLine()
        if (!input?.trim()) {
            return defaultValue
        }
        return input.trim()
    }

    static void runCommand(String command) {
        println "> ${command}"
        def proc = ['bash', '-lc', command].execute()
        proc.in.eachLine { println it }
        proc.err.eachLine { System.err.println it }
        proc.waitFor()
        if (proc.exitValue() != 0) {
            println "Befehl fehlgeschlagen: ${command}"
            System.exit(proc.exitValue())
        }
    }
}
