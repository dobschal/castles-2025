<template>
  <div class="building-tile">
    <img src="@/assets/tiles/village.png" class="building" alt="Building" />
  </div>
</template>

<style lang="scss" scoped>
.building-tile {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;

  .building {
    width: 150%;
    margin-left: -25%;
    margin-top: -25%;
  }
}
</style>
