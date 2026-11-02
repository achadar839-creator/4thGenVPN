import com.wireguard.config.Config
import com.wireguard.config.Interface
import com.wireguard.config.Peer

object VpnEngine {
    fun buildConfig(server: VpnServer): Config {
        // फोन का इंटरफेस (Client) सेट करें
        val interfaceBuilder = Interface.Builder()
        interfaceBuilder.parsePrivateKey(server.privKey)
        interfaceBuilder.parseAddresses(server.ip)
        interfaceBuilder.parseDnsServers("10.2.0.1")

        // प्रोटॉन सर्वर (Peer) सेट करें
        val peerBuilder = Peer.Builder()
        peerBuilder.parsePublicKey(server.pubKey)
        peerBuilder.parseEndpoint(server.endpoint)
        peerBuilder.parseAllowedIPs("0.0.0.0/0") // सारा ट्रैफिक VPN से जाएगा

        // कनेक्शन प्रोफाइल रेडी करें
        return Config.Builder()
            .setInterface(interfaceBuilder.build())
            .addPeer(peerBuilder.build())
            .build()
    }
}
