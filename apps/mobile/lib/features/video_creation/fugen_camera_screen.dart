import 'dart:io';

import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:video_player/video_player.dart';

enum FugenEffect {
  natural,
  warm,
  cool,
  vintage,
  mono,
  glow,
  soft,
}

extension FugenEffectStyle on FugenEffect {
  String get label => switch (this) {
        FugenEffect.natural => 'Natural',
        FugenEffect.warm => 'Warm',
        FugenEffect.cool => 'Cool',
        FugenEffect.vintage => 'Vintage',
        FugenEffect.mono => 'Mono',
        FugenEffect.glow => 'Glow',
        FugenEffect.soft => 'Soft',
      };

  ColorFilter get colorFilter => switch (this) {
        FugenEffect.natural => const ColorFilter.mode(
            Colors.transparent,
            BlendMode.dst,
          ),
        FugenEffect.warm => const ColorFilter.matrix([
            1.08, 0, 0, 0, 8,
            0, 1.02, 0, 0, 3,
            0, 0, 0.88, 0, 0,
            0, 0, 0, 1, 0,
          ]),
        FugenEffect.cool => const ColorFilter.matrix([
            0.92, 0, 0, 0, 0,
            0, 1.01, 0, 0, 0,
            0, 0, 1.12, 0, 5,
            0, 0, 0, 1, 0,
          ]),
        FugenEffect.vintage => const ColorFilter.matrix([
            0.85, 0.08, 0.02, 0, 12,
            0.04, 0.90, 0.03, 0, 6,
            0.02, 0.06, 0.72, 0, 0,
            0, 0, 0, 1, 0,
          ]),
        FugenEffect.mono => const ColorFilter.matrix([
            0.213, 0.715, 0.072, 0, 0,
            0.213, 0.715, 0.072, 0, 0,
            0.213, 0.715, 0.072, 0, 0,
            0, 0, 0, 1, 0,
          ]),
        FugenEffect.glow || FugenEffect.soft =>
          const ColorFilter.mode(Colors.transparent, BlendMode.dst),
      };
}

class FugenCameraScreen extends StatefulWidget {
  const FugenCameraScreen({
    super.key,
    this.onRecorded,
  });

  final ValueChanged<String>? onRecorded;

  @override
  State<FugenCameraScreen> createState() => _FugenCameraScreenState();
}

class _FugenCameraScreenState extends State<FugenCameraScreen>
    with WidgetsBindingObserver {
  CameraController? _controller;
  List<CameraDescription> _cameras = [];

  FugenEffect _effect = FugenEffect.natural;
  bool _recording = false;
  bool _busy = false;
  bool _initializing = true;
  String? _error;
  String? _recordedPath;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _initializeCamera();
  }

  Future<void> _initializeCamera({
    CameraDescription? preferred,
  }) async {
    if (mounted) {
      setState(() {
        _initializing = true;
        _error = null;
      });
    }

    CameraController? next;

    try {
      _cameras = await availableCameras();

      if (_cameras.isEmpty) {
        throw Exception('No camera is available on this device.');
      }

      final camera = preferred ??
          _cameras.firstWhere(
            (c) => c.lensDirection == CameraLensDirection.back,
            orElse: () => _cameras.first,
          );

      next = CameraController(
        camera,
        ResolutionPreset.high,
        enableAudio: true,
        imageFormatGroup: ImageFormatGroup.yuv420,
      );

      await next.initialize();

      if (!mounted) {
        await next.dispose();
        return;
      }

      final old = _controller;
      setState(() {
        _controller = next;
        _initializing = false;
      });

      if (old != null) {
        await old.dispose();
      }
    } catch (e) {
      if (next != null && next != _controller) {
        await next.dispose();
      }

      if (mounted) {
        setState(() {
          _initializing = false;
          _error = e.toString();
        });
      }
    }
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.inactive ||
        state == AppLifecycleState.paused) {
      final old = _controller;
      _controller = null;

      if (mounted) {
        setState(() {});
      }

      old?.dispose();
    } else if (state == AppLifecycleState.resumed &&
        !_recording &&
        !_busy) {
      _initializeCamera();
    }
  }

  Future<void> _switchCamera() async {
    if (_recording || _busy || _cameras.length < 2) return;

    final current = _controller?.description;
    if (current == null) return;

    final desired = current.lensDirection == CameraLensDirection.front
        ? CameraLensDirection.back
        : CameraLensDirection.front;

    final target = _cameras.where(
      (c) => c.lensDirection == desired,
    );

    if (target.isNotEmpty) {
      await _initializeCamera(preferred: target.first);
    }
  }

  Future<void> _toggleRecording() async {
    final camera = _controller;

    if (camera == null ||
        !camera.value.isInitialized ||
        _busy) {
      return;
    }

    setState(() => _busy = true);

    try {
      if (!_recording) {
        await camera.startVideoRecording();

        if (!mounted) return;

        setState(() {
          _recording = true;
          _busy = false;
          _recordedPath = null;
        });
      } else {
        final result = await camera.stopVideoRecording();

        final directory = await getApplicationSupportDirectory();
        final recordings = Directory(
          '${directory.path}/fugen_recordings',
        );

        await recordings.create(recursive: true);

        final destination =
            '${recordings.path}/fugen_${DateTime.now().millisecondsSinceEpoch}.mp4';

        final saved = await File(result.path).copy(destination);

        if (!mounted) return;

        setState(() {
          _recording = false;
          _busy = false;
          _recordedPath = saved.path;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _recording = false;
          _busy = false;
          _error = e.toString();
        });
      }
    }
  }

  Widget _buildPreview() {
    final camera = _controller!;

    Widget preview = CameraPreview(camera);

    return ColorFiltered(
      colorFilter: _effect.colorFilter,
      child: preview,
    );
  }

  @override
  Widget build(BuildContext context) {
    final camera = _controller;

    return Scaffold(
      backgroundColor: Colors.black,
      body: SafeArea(
        child: Stack(
          fit: StackFit.expand,
          children: [
            if (_initializing)
              const Center(child: CircularProgressIndicator())
            else if (_error != null)
              Center(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        _error!,
                        style: const TextStyle(color: Colors.white),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 16),
                      FilledButton(
                        onPressed: _initializeCamera,
                        child: const Text('Retry camera'),
                      ),
                    ],
                  ),
                ),
              )
            else if (camera != null && camera.value.isInitialized)
              Center(child: _buildPreview()),

            Positioned(
              top: 12,
              left: 16,
              right: 16,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  IconButton(
                    onPressed: _recording || _busy
                        ? null
                        : () => Navigator.maybePop(context),
                    icon: const Icon(Icons.close, color: Colors.white),
                  ),
                  if (_recording)
                    const Text(
                      'REC',
                      style: TextStyle(
                        color: Colors.redAccent,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  IconButton(
                    onPressed: _switchCamera,
                    icon: const Icon(
                      Icons.cameraswitch,
                      color: Colors.white,
                    ),
                  ),
                ],
              ),
            ),

            Positioned(
              bottom: 150,
              left: 0,
              right: 0,
              child: SizedBox(
                height: 64,
                child: ListView(
                  scrollDirection: Axis.horizontal,
                  padding: const EdgeInsets.symmetric(horizontal: 12),
                  children: FugenEffect.values.map((effect) {
                    final selected = effect == _effect;

                    return Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 5),
                      child: ChoiceChip(
                        label: Text(effect.label),
                        selected: selected,
                        onSelected: _recording || _busy
                            ? null
                            : (_) => setState(() => _effect = effect),
                      ),
                    );
                  }).toList(),
                ),
              ),
            ),

            Positioned(
              bottom: 55,
              left: 0,
              right: 0,
              child: Center(
                child: GestureDetector(
                  onTap: _toggleRecording,
                  child: Container(
                    width: 76,
                    height: 76,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      border: Border.all(
                        color: Colors.white,
                        width: 5,
                      ),
                    ),
                    child: Center(
                      child: Container(
                        width: _recording ? 28 : 58,
                        height: _recording ? 28 : 58,
                        decoration: BoxDecoration(
                          color: _recording
                              ? Colors.red
                              : Colors.white,
                          borderRadius: BorderRadius.circular(
                            _recording ? 6 : 40,
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ),

            if (_recordedPath != null && !_recording)
              Positioned(
                bottom: 12,
                right: 16,
                child: FilledButton.icon(
                  onPressed: () {
                    Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (_) => FugenVideoReviewScreen(
                          path: _recordedPath!,
                          onUseVideo: widget.onRecorded,
                        ),
                      ),
                    );
                  },
                  icon: const Icon(Icons.arrow_forward),
                  label: const Text('Preview video'),
                ),
              ),

            if (_busy)
              const Positioned(
                bottom: 16,
                left: 16,
                child: CircularProgressIndicator(),
              ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _controller?.dispose();
    super.dispose();
  }
}

class FugenVideoReviewScreen extends StatefulWidget {
  const FugenVideoReviewScreen({
    super.key,
    required this.path,
    this.onUseVideo,
  });

  final String path;
  final ValueChanged<String>? onUseVideo;

  @override
  State<FugenVideoReviewScreen> createState() =>
      _FugenVideoReviewScreenState();
}

class _FugenVideoReviewScreenState extends State<FugenVideoReviewScreen> {
  late final VideoPlayerController _player;
  String? _error;

  @override
  void initState() {
    super.initState();
    _player = VideoPlayerController.file(File(widget.path))
      ..initialize().then((_) {
        if (mounted) setState(() {});
      }).catchError((Object e) {
        if (mounted) setState(() => _error = e.toString());
      });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Review video')),
      body: Center(
        child: _error != null
            ? Text(_error!)
            : _player.value.isInitialized
                ? AspectRatio(
                    aspectRatio: _player.value.aspectRatio,
                    child: VideoPlayer(_player),
                  )
                : const CircularProgressIndicator(),
      ),
      floatingActionButton: _player.value.isInitialized
          ? FloatingActionButton(
              onPressed: () {
                setState(() {
                  _player.value.isPlaying
                      ? _player.pause()
                      : _player.play();
                });
              },
              child: Icon(
                _player.value.isPlaying
                    ? Icons.pause
                    : Icons.play_arrow,
              ),
            )
          : null,
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: FilledButton(
            onPressed: () {
              widget.onUseVideo?.call(widget.path);
              Navigator.pop(context);
            },
            child: const Text('Use this video'),
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _player.dispose();
    super.dispose();
  }
}
