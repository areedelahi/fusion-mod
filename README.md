<h1 align="center">Fusion</h1>

<p align="center">
  A NeoForge mod for Minecraft 1.21.4 that allows two players to <strong>fuse into a single shared entity</strong> via a unique Siamese-style system.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.4-62B47A?style=flat-square&logo=mojangstudios&logoColor=white" alt="Minecraft 1.21.4"/>
  <img src="https://img.shields.io/badge/NeoForge-21.4.138-orange?style=flat-square" alt="NeoForge"/>
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21"/>
</p>

---

## 🧬 The Fusion Experience

Fusion creates a 1:1 bond between two players. One acts as the **Host** (the physical body) while the other becomes the **Guest** (an invisible, attached presence). Together, you function as a single unit with synchronized mechanics across the entire mod.

### ✨ Key Features

| Feature | Description |
|---------|-------------|
| **Fusion** | One player's body is shared; the other is securely attached and invisible. |
| **Combined Movement** | Real-time synchronization of both players' inputs to control one body. |
| **Shared Vitals** | Combined health pool, shared hunger, and unified status effects. |
| **Composite Skins** | Dynamic texture merging (side-by-side or back-to-back) based on your chosen orientation. |
| **Bidirectional Sync** | Full parity for inventory, mouse movement, and keyboard actions. |
| **Persistence** | Fusions survive server restarts, player disconnects, and dimension travel. |
| **Spectator Mode** | Guests can observe the shared world perfectly without collision issues. |

---

## 🎮 Getting Started

### Commands
All management commands require **Operator level 2** (except `/fusion status`).

| Command | Usage |
|---------|-------|
| `/fusion fuse <host> <guest>` | Fuses two players together. |
| `/fusion unfuse [player]` | Dissolves a fusion (defaults to yourself). |
| `/fusion status [player]` | Shows current fusion state and partners. |
| `/fusion setmode <mode>` | Changes the movement control mode. |

### Control Modes
Tailor your fusion experience to your playstyle:

*   **`shared` (Default):** Both players' movement inputs are averaged. Coordination is key!
*   **`passenger`:** Only the Host controls movement; the Guest enjoys the ride.
*   **`override`:** The Host can override the Guest's input by moving; otherwise, they combine.

### Body Orientations
*   **`side_by_side`:** Merges the left half of the Host's skin with the right half of the Guest's.
*   **`back_to_back`:** Merges the front of the Host's skin with the back of the Guest's.

---

## 🛠️ Installation & Building

### Standard Installation
1. Install [NeoForge](https://neoforged.net/) for Minecraft **1.21.4**.
2. Download the `fusion.jar` from the GitHub releases.
3. Place the JAR into your `.minecraft/mods/` folder.

### Building from Source
If you want to build the latest dev version:
```bash
git clone https://github.com/your-org/fusion.git
cd fusion
./gradlew build
```
The JAR will be located in `build/libs/`.

---

## 🛣️ Roadmap
- [ ] **Infection System:** Spread the fusion through combat or specific items.
- [ ] **Absorption:** One player can temporarily "consume" another for a power boost.
- [ ] **Fused Combat Hooks:** Enhanced interaction routing for both players.
- [ ] **UI Overhaul:** Dedicated HUD elements for shared status and fusion progress.

---

<p align="center">
  Built with passion for the NeoForge community.
</p>
