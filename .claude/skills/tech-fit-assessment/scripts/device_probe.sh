#!/bin/bash
# Probes a connected Android device and, optionally, an installed app. Read only: changes nothing on the device.
# Usage: device_probe.sh [-s <serial>] [<package>]
# Output is plain text for the "processing power" section of a technology fit assessment.

export MSYS_NO_PATHCONV=1
ADB="${ADB:-adb}"
[ -x "/c/Users/crbra/AppData/Local/Android/Sdk/platform-tools/adb.exe" ] && ADB="/c/Users/crbra/AppData/Local/Android/Sdk/platform-tools/adb.exe"
SERIAL=""
if [ "$1" = "-s" ]; then SERIAL="-s $2"; shift 2; fi
PKG="$1"
sh() { "$ADB" $SERIAL shell "$@" 2>/dev/null | tr -d '\r'; }

if [ -z "$("$ADB" $SERIAL get-state 2>/dev/null | tr -d '\r')" ]; then
  echo "No device found. Connect the phone with USB debugging on, then run adb devices."; exit 1
fi

echo "== Device"
echo "model:      $(sh getprop ro.product.manufacturer) $(sh getprop ro.product.model)"
echo "android:    $(sh getprop ro.build.version.release) (API $(sh getprop ro.build.version.sdk))"
echo "abis:       $(sh getprop ro.product.cpu.abilist)"
echo "soc:        $(sh getprop ro.soc.manufacturer) $(sh getprop ro.soc.model) | board $(sh getprop ro.board.platform) | hardware $(sh getprop ro.hardware)"

echo "== CPU"
echo "cores:      $(sh "grep -c ^processor /proc/cpuinfo")"
for f in $(sh "ls /sys/devices/system/cpu/ | grep -E '^cpu[0-9]+$'"); do
  max=$(sh "cat /sys/devices/system/cpu/$f/cpufreq/cpuinfo_max_freq")
  [ -n "$max" ] && echo "$f max kHz: $max"
done
sh "grep -m3 -E 'Hardware|model name|CPU part' /proc/cpuinfo"

echo "== Memory (kB)"
sh "grep -E 'MemTotal|MemFree|MemAvailable|SwapTotal' /proc/meminfo"

echo "== Storage"
sh "df -h /data | tail -1"

echo "== Battery and heat"
sh "dumpsys battery | grep -E 'level|temperature|status|powered'"
sh "dumpsys thermalservice | grep -m2 -i 'Thermal Status'"

if [ -n "$PKG" ]; then
  echo "== App $PKG"
  sh "pm path $PKG"
  for p in $(sh "pm path $PKG" | sed 's/package://'); do echo "file size: $(sh "stat -c %s $p") bytes  $p"; done
  sh "dumpsys meminfo $PKG | grep -E 'TOTAL PSS|Native Heap|Java Heap|Code:|Graphics:' | head -6"
  echo "data size: $(sh "du -sk /data/data/$PKG 2>/dev/null | cut -f1") kB (may be empty without run-as)"
fi
