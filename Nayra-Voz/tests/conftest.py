import os
import sys
from pathlib import Path

os.environ.setdefault("NAYRA_VOZ_AUTOARRANQUE", "0")
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
