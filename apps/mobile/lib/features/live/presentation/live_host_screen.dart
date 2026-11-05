import 'package:flutter/material.dart';
import 'package:livekit_client/livekit_client.dart';
import '../services/livekit_service.dart';

class LiveHostScreen extends StatefulWidget {
  final String wsUrl;
  final String token;

  const LiveHostScreen({Key? key, required this.wsUrl, required this.token}) : super(key: key);

  @override
  State<LiveHostScreen> createState() => _LiveHostScreenState();
}

class _LiveHostScreenState extends State<LiveHostScreen> {
  final LiveKitService _liveKitService = LiveKitService();
  bool _isLoading = true;
  bool _isMuted = false;
  bool _isCameraOff = false;

  @override
  void initState() {
    super.initState();
    _initLive();
  }

  Future<void> _initLive() async {
    try {
      await _liveKitService.connectToRoom(wsUrl: widget.wsUrl, token: widget.token);
      setState(() {
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  void dispose() {
    _liveKitService.disconnect();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          // Local Camera Preview Renderer
          if (!_isLoading && _liveKitService.localVideoTrack != null && !_isCameraOff)
            Positioned.fill(
              child: VideoTrackRenderer(_liveKitService.localVideoTrack!),
            )
          else
            const Center(
              child: CircularProgressIndicator(color: Color(0xFFFE2C55)),
            ),

          // Top Header Bar
          Positioned(
            top: 50,
            left: 16,
            right: 16,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    color: const Color(0xFFFE2C55),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: const Text(
                    'LIVE 🔴',
                    style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.close, color: Colors.white),
                  onPressed: () => Navigator.pop(context),
                ),
              ],
            ),
          ),

          // Bottom Controls (Mute, Camera Flip, End Live)
          Positioned(
            bottom: 40,
            left: 20,
            right: 20,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                IconButton(
                  icon: Icon(_isMuted ? Icons.mic_off : Icons.mic, color: Colors.white),
                  onPressed: () {
                    setState(() {
                      _isMuted = !_isMuted;
                      _liveKitService.toggleMicrophone(_isMuted);
                    });
                  },
                ),
                IconButton(
                  icon: Icon(_isCameraOff ? Icons.videocam_off : Icons.videocam, color: Colors.white),
                  onPressed: () {
                    setState(() {
                      _isCameraOff = !_isCameraOff;
                      _liveKitService.toggleCamera(!_isCameraOff);
                    });
                  },
                ),
                IconButton(
                  icon: const Icon(Icons.switch_camera, color: Colors.white),
                  onPressed: () => _liveKitService.switchCamera(),
                ),
                ElevatedButton(
                  style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                  onPressed: () => Navigator.pop(context),
                  child: const Text('End LIVE', style: TextStyle(color: Colors.white)),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
